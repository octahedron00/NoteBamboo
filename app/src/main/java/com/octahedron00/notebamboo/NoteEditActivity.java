package com.octahedron00.notebamboo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class NoteEditActivity extends AppCompatActivity {
	byte[] AES_key;
	int note, level, parent, length, add;
	String beforeTitle, beforeText, baseText, baseTitle;
	boolean isMerge;

	ImageView imageBack, imageSubmit;
	EditText titleView;
	TextEditor textInput;
	Button orient, bold, italic, underl, highlight, plus;
	ScrollView editBack;
	LinearLayout colorSelect, editBack2;
	ConstraintLayout color1, color2, color3, color4, color5, color6, color7, color8, color9, color0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_edit);

		Intent intent = getIntent();
		note = intent.getIntExtra("note",0);
		add = intent.getIntExtra("add",0);
		level = intent.getIntExtra("level", 0);
		isMerge = intent.getBooleanExtra("merge", false);
		AES_key = intent.getByteArrayExtra("AES_key");
		baseText = intent.getStringExtra("baseText");
		baseTitle = intent.getStringExtra("baseTitle");

		titleView = findViewById(R.id.titleView);
		textInput = findViewById(R.id.textInput);
		imageBack = findViewById(R.id.imageBack);
		editBack = findViewById(R.id.editBackground);
		editBack2 = findViewById(R.id.editBack);
		imageSubmit = findViewById(R.id.imageSubmit);
		colorSelect = findViewById(R.id.colorSelect);
		color1 = findViewById(R.id.colorLayout1);
		color2 = findViewById(R.id.colorLayout2);
		color3 = findViewById(R.id.colorLayout3);
		color4 = findViewById(R.id.colorLayout4);
		color5 = findViewById(R.id.colorLayout5);
		color6 = findViewById(R.id.colorLayout6);
		color7 = findViewById(R.id.colorLayout7);
		color8 = findViewById(R.id.colorLayout8);
		color9 = findViewById(R.id.colorLayout9);
		color0 = findViewById(R.id.colorLayout0);

		textInput.requestFocus();

		colorSelect.setVisibility(View.GONE);

		imageBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				back();
			}
		});
		imageSubmit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				submit();
			}
		});

		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d("tag", "onResponse: "+response);
				try {
					JSONObject object = new JSONObject(response);
					if(!object.getBoolean("success")){
						finish();
					}
					SecretKey key = new SecretKeySpec(AES_key, "AES");
					Cipher decode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
					decode.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(Data.iv_def));
					final String title = new String(decode.doFinal(Base64.decode(object.getString("title"),Base64.NO_WRAP)));
					final String text = new String(decode.doFinal(Base64.decode(object.getString("text"), Base64.NO_WRAP)));
					beforeText = text;
					beforeTitle = title;

					parent = object.getInt("no");
					length = object.getInt("length");
					length++;

					titleView.setText(title);

					if(isMerge){
						textInput.setText(merge(text, baseText, title, baseTitle, true));
					}
					else{
						textInput.setText(Html.fromHtml(text));
					}

				} catch (JSONException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
					e.printStackTrace();
					finish();
				}
			}
		};
		Map<String, String> endMap = new HashMap<>();
		endMap.put("method", "note_end");
		endMap.put("note", Integer.toString(note));
		AllRequest request = new AllRequest(endMap, listener);
		RequestQueue queue = Volley.newRequestQueue(this);
		queue.add(request);

		orient = findViewById(R.id.orient);
		orient.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(0);
			}
		});
		bold = findViewById(R.id.bold);
		bold.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(1);
			}
		});
		italic = findViewById(R.id.italic);
		italic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(2);
			}
		});
		underl = findViewById(R.id.underl);
		underl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(3);
			}
		});
		plus = findViewById(R.id.plus);
		plus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(4);
			}
		});
		highlight = findViewById(R.id.highlight);
		highlight.setOnClickListener(new View.OnClickListener() {
			boolean isOpen = false;
			@Override
			public void onClick(View v) {
				if(isOpen){
					colorSelect.setVisibility(View.GONE);
					isOpen = false;
				}
				else{
					colorSelect.setVisibility(View.VISIBLE);
					isOpen = true;
				}
			}
		});
		color1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(0xFFB980F0);
			}
		});
		color2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(0xFFE53A40);
			}
		});
		color3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(0xFFFC913A);
			}
		});
		color4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(0xFFEFEC05);
			}
		});
		color5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(0xFF75D701);
			}
		});
		color6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(0xFF33B9F1);
			}
		});
		color7.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(0xFF0080FF);
			}
		});
		color8.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(0xFFFAFAFA);
			}
		});
		color9.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(0xFFAAAAAA);
			}
		});
		color0.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				edit(0xFF000000);
			}
		});

		editBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e("TAG", "onClick: focused?");
				textInput.requestFocus();
			}
		});
		editBack2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e("TAG", "onClick: focused??");
				textInput.requestFocus();
			}
		});
	}

	@Override
	public void onBackPressed() {
		back();
	}

	public void edit(int a){
		int ss = textInput.getSelectionStart();
		int se = textInput.getSelectionEnd();
		final Spannable span = textInput.getText();


		Log.i("edit_span", "edit: "+Integer.toHexString(a));

		StyleSpan[] styleSpans = span.getSpans(ss, se, StyleSpan.class);
		Arrays.sort(styleSpans, new Comparator<StyleSpan>() {
			@Override
			public int compare(StyleSpan o1, StyleSpan o2) {
				if(span.getSpanStart(o1)<span.getSpanStart(o2)){
					return 1;
				}
				return 0;
			}
		});

		UnderlineSpan[] underlineSpans = span.getSpans(ss, se, UnderlineSpan.class);
		Arrays.sort(underlineSpans, new Comparator<UnderlineSpan>() {
			@Override
			public int compare(UnderlineSpan o1, UnderlineSpan o2) {
				if(span.getSpanStart(o1)<span.getSpanStart(o2)){
					return 1;
				}
				return 0;
			}
		});

		StrikethroughSpan[] strikethroughSpans = span.getSpans(ss, se, StrikethroughSpan.class);
		Arrays.sort(strikethroughSpans, new Comparator<StrikethroughSpan>() {
			@Override
			public int compare(StrikethroughSpan o1, StrikethroughSpan o2) {
				if(span.getSpanStart(o1)<span.getSpanStart(o2)){
					return 1;
				}
				return 0;
			}
		});

		ForegroundColorSpan[] foregroundColorSpans = span.getSpans(ss, se, ForegroundColorSpan.class);
		Arrays.sort(foregroundColorSpans, new Comparator<ForegroundColorSpan>() {
			@Override
			public int compare(ForegroundColorSpan o1, ForegroundColorSpan o2) {
				if(span.getSpanStart(o1)<span.getSpanStart(o2)){
					return 1;
				}
				return 0;
			}
		});

		BackgroundColorSpan[] backgroundColorSpans = span.getSpans(ss, se, BackgroundColorSpan.class);
		Arrays.sort(backgroundColorSpans, new Comparator<BackgroundColorSpan>() {
			@Override
			public int compare(BackgroundColorSpan o1, BackgroundColorSpan o2) {
				if(span.getSpanStart(o1)<span.getSpanStart(o2)){
					return 1;
				}
				return 0;
			}
		});

		for(int i=0; i<styleSpans.length; i++){
			int s, e;
			s = span.getSpanStart(styleSpans[i]);
			e = span.getSpanEnd(styleSpans[i]);
			Log.i("edit_span style", "edit: "+i+" / "+styleSpans[i].toString()+"/"+styleSpans[i].getStyle()+"/ "+s+"~"+e);
		}
		for(int i=0; i<underlineSpans.length; i++){
			int s, e;
			s = span.getSpanStart(underlineSpans[i]);
			e = span.getSpanEnd(underlineSpans[i]);
			Log.i("edit_span under", "edit: "+i+" / "+underlineSpans[i].toString()+"/"+"/ "+s+"~"+e);
		}
		for(int i=0; i<strikethroughSpans.length; i++){
			int s, e;
			s = span.getSpanStart(strikethroughSpans[i]);
			e = span.getSpanEnd(strikethroughSpans[i]);
			Log.i("edit_span strTh", "edit: "+i+" / "+strikethroughSpans[i].toString()+"// "+s+"~"+e);
		}
		for(int i=0; i<foregroundColorSpans.length; i++){
			int s, e;
			s = span.getSpanStart(foregroundColorSpans[i]);
			e = span.getSpanEnd(foregroundColorSpans[i]);
			Log.i("edit_span foreG", "edit: "+i+" / "+foregroundColorSpans[i].toString()+"/"+foregroundColorSpans[i].getForegroundColor()+"/ "+s+"~"+e);
		}
		for(int i=0; i<backgroundColorSpans.length; i++) {
			int s, e;
			s = span.getSpanStart(backgroundColorSpans[i]);
			e = span.getSpanEnd(backgroundColorSpans[i]);
			Log.i("edit_span backG", "edit: " + i + " / " + backgroundColorSpans[i].toString() + "/" + backgroundColorSpans[i].getBackgroundColor() + "/ " + s + "~" + e);
		}

		if(a==0){
			for(int i=0; i<styleSpans.length; i++){
				int s, e;
				s = span.getSpanStart(styleSpans[i]);
				e = span.getSpanEnd(styleSpans[i]);
				span.removeSpan(styleSpans[i]);
				if(s<ss){
					span.setSpan(new StyleSpan(styleSpans[i].getStyle()), s, ss, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
				if(se<e){
					span.setSpan(new StyleSpan(styleSpans[i].getStyle()), se, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
			}
			for(int i=0; i<underlineSpans.length; i++){
				int s, e;
				s = span.getSpanStart(underlineSpans[i]);
				e = span.getSpanEnd(underlineSpans[i]);
				span.removeSpan(underlineSpans[i]);
				if(s<ss){
					span.setSpan(new UnderlineSpan(), s, ss, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
				if(se<e){
					span.setSpan(new UnderlineSpan(), se, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
			}
			for(int i=0; i<strikethroughSpans.length; i++){
				int s, e;
				s = span.getSpanStart(strikethroughSpans[i]);
				e = span.getSpanEnd(strikethroughSpans[i]);
				span.removeSpan(strikethroughSpans[i]);
				if(s<ss){
					span.setSpan(new StrikethroughSpan(), s, ss, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
				if(se<e){
					span.setSpan(new StrikethroughSpan(), se, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
			}
			for(int i=0; i<foregroundColorSpans.length; i++){
				int s, e;
				s = span.getSpanStart(foregroundColorSpans[i]);
				e = span.getSpanEnd(foregroundColorSpans[i]);
				span.removeSpan(foregroundColorSpans[i]);
				if(s<ss){
					span.setSpan(new ForegroundColorSpan(foregroundColorSpans[i].getForegroundColor()), s, ss, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
				if(se<e){
					span.setSpan(new ForegroundColorSpan(foregroundColorSpans[i].getForegroundColor()), se, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
			}
			for(int i=0; i<backgroundColorSpans.length; i++){
				int s, e;
				s = span.getSpanStart(backgroundColorSpans[i]);
				e = span.getSpanEnd(backgroundColorSpans[i]);
				span.removeSpan(backgroundColorSpans[i]);
				if(s<ss){
					span.setSpan(new BackgroundColorSpan(backgroundColorSpans[i].getBackgroundColor()), s, ss, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
				if(se<e){
					span.setSpan(new BackgroundColorSpan(backgroundColorSpans[i].getBackgroundColor()), se, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
			}
		}

		int s1=655360, e1=ss;
		if(a==1){
			if(styleSpans.length==0){
				span.setSpan(new StyleSpan(Typeface.BOLD), ss, se, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				return;
			}
			for(int i=0; i<styleSpans.length; i++){
				if(styleSpans[i].getStyle()!=Typeface.BOLD){
					continue;
				}
				int s, e;
				s = span.getSpanStart(styleSpans[i]);
				e = span.getSpanEnd(styleSpans[i]);
				if(s<=s1){
					s1 = s;
				}
				if(s<=e1&&e1<=e){
					e1 = e;
				}
				span.removeSpan(styleSpans[i]);
				if(s<ss){
					span.setSpan(new StyleSpan(styleSpans[i].getStyle()), s, ss, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
				if(se<e){
					span.setSpan(new StyleSpan(styleSpans[i].getStyle()), se, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
			}
			if (s1 > ss || se > e1) {
				span.setSpan(new StyleSpan(Typeface.BOLD), ss, se, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
			}
		}

		if(a==2){
			if(styleSpans.length==0){
				span.setSpan(new StyleSpan(Typeface.ITALIC), ss, se, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				return;
			}
			for(int i=0; i<styleSpans.length; i++){
				if(styleSpans[i].getStyle()!=Typeface.ITALIC){
					continue;
				}
				int s, e;
				s = span.getSpanStart(styleSpans[i]);
				e = span.getSpanEnd(styleSpans[i]);
				if(s<=s1){
					s1 = s;
				}
				if(s<=e1&&e1<=e){
					e1 = e;
				}
				span.removeSpan(styleSpans[i]);
				if(s<ss){
					span.setSpan(new StyleSpan(styleSpans[i].getStyle()), s, ss, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
				if(se<e){
					span.setSpan(new StyleSpan(styleSpans[i].getStyle()), se, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
			}
			if (s1 > ss || se > e1) {
				span.setSpan(new StyleSpan(Typeface.ITALIC), ss, se, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
			}
		}

		if(a==3){
			for(int i=0; i<underlineSpans.length; i++){
				int s, e;
				s = span.getSpanStart(underlineSpans[i]);
				e = span.getSpanEnd(underlineSpans[i]);
				if(s<=s1){
					s1 = s;
				}
				if(s<=e1&&e1<=e){
					e1 = e;
				}
				span.removeSpan(underlineSpans[i]);
				if(s<ss){
					span.setSpan(new UnderlineSpan(), s, ss, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
				if(se<e){
					span.setSpan(new UnderlineSpan(), se, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
			}
			if (s1 > ss || se > e1) {
				span.setSpan(new UnderlineSpan(), ss, se, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
			}
		}

		if(a==4){
			for(int i=0; i<strikethroughSpans.length; i++){
				int s, e;
				s = span.getSpanStart(strikethroughSpans[i]);
				e = span.getSpanEnd(strikethroughSpans[i]);
				if(s<=s1){
					s1 = s;
				}
				if(s<=e1&&e1<=e){
					e1 = e;
				}
				span.removeSpan(strikethroughSpans[i]);
				if(s<ss){
					span.setSpan(new StrikethroughSpan(), s, ss, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
				if(se<e){
					span.setSpan(new StrikethroughSpan(), se, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
			}
			if (s1 > ss || se > e1) {
				span.setSpan(new StrikethroughSpan(), ss, se, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
			}
		}

		if(a>10||a<0){
			for(int i=0; i<foregroundColorSpans.length; i++){
				int s, e;
				s = span.getSpanStart(foregroundColorSpans[i]);
				e = span.getSpanEnd(foregroundColorSpans[i]);
				span.removeSpan(foregroundColorSpans[i]);
				if(s<ss){
					span.setSpan(new ForegroundColorSpan(foregroundColorSpans[i].getForegroundColor()), s, ss, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
				if(se<e){
					span.setSpan(new ForegroundColorSpan(foregroundColorSpans[i].getForegroundColor()), se, e, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				}
			}
			span.setSpan(new ForegroundColorSpan(a), ss, se, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		}

		Log.e("TAG", "edit span: "+ss+"~"+se+"/"+s1+"~"+e1);
		return;
	}

	public void submit(){
		Log.d("tags", "titleview  : "+titleView.getText().toString());
		Log.d("tags", "titlebefore: "+beforeTitle);
		Log.d("tags", "textview   : "+Html.toHtml(textInput.getText()));
		Log.d("tags", "textbefore : "+beforeText);
		Log.e("tags", "title      : "+titleView.getText().toString().equals(beforeTitle));
		Log.e("tags", "text       : "+Html.toHtml(textInput.getText()).equals(beforeText));

		SpannableStringBuilder stringBuilder = new SpannableStringBuilder(textInput.getText());
		if(stringBuilder.charAt(stringBuilder.length()-1)!='\n'){
			stringBuilder = stringBuilder.append("\n");
		}

		BackgroundColorSpan[] spans = stringBuilder.getSpans(0, stringBuilder.length(), BackgroundColorSpan.class);
		Log.e("TAG", "spans_background_delete");
		for(int i=0; i<spans.length; i++){
			Log.e("TAG", "spans_background_delete"+i+" / "+spans[i].toString());
			stringBuilder.removeSpan(spans[i]);
		}

		StringBuilder textBuilder = new StringBuilder();

		int s = 0, t = 0;
		for(int i=0; i<stringBuilder.length(); i++){
			if(stringBuilder.charAt(i)=='\n'){
				t++;
				String fragment = Html.toHtml(new SpannableString(stringBuilder.subSequence(s, i)));
				String bottom = "";
				if(fragment.length()>18){
					bottom = fragment.substring(13, (fragment.length()-5));
				}
				s = i+1;
				textBuilder.append(bottom+"<br>\n");
			}
		}


		if(titleView.getText().toString().length()<1){
			Toast.makeText(this, "Input the title", Toast.LENGTH_SHORT).show();
			return;
		}
		if(titleView.getText().toString().length()>20){
			Toast.makeText(this, "Title must be shorter than 20", Toast.LENGTH_SHORT).show();
			return;
		}
		if(textInput.getText().toString().length()>65536){
			Toast.makeText(this, "One note must be shorter than 65536", Toast.LENGTH_SHORT).show();
			return;
		}
		if(t>1024){
			Toast.makeText(this, "One note must be smaller than 1024 lines", Toast.LENGTH_SHORT).show();
			return;
		}
		if(titleView.getText().toString().equals(beforeTitle)&&Html.toHtml(textInput.getText()).equals(Html.toHtml(Html.fromHtml(beforeText)))){
			finish();
			return;
		}

		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d("tag",response);
				finish();
			}
		};

		try {

			SecretKey key = new SecretKeySpec(AES_key, "AES");
			Cipher encode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			encode.init(Cipher.ENCRYPT_MODE,key,new IvParameterSpec(Data.iv_def));
			final String title = Base64.encodeToString(encode.doFinal(titleView.getText().toString().getBytes()),Base64.NO_WRAP);
			final String text = Base64.encodeToString(encode.doFinal(textBuilder.toString().getBytes()),Base64.NO_WRAP);
			Log.d("tag","private_dec " + text);

			Map<String, String> map = new HashMap<>();
			map.put("method", "note_save");
			map.put("note", Integer.toString(note));
			map.put("user", Integer.toString(Data.user_no));
			map.put("parent", Integer.toString(parent));
			map.put("length", Integer.toString(length));
			map.put("text", text);
			map.put("title", title);
			if(isMerge){
				map.put("add", Integer.toString(add));
			}
			AllRequest request = new AllRequest(map, listener);
			RequestQueue queue = Volley.newRequestQueue(NoteEditActivity.this);
			queue.add(request);
		} catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
			e.printStackTrace();
		}
	}

	public void back(){
		Log.d("tags", "titleview  : "+titleView.getText().toString());
		Log.d("tags", "titlebefore: "+beforeTitle);
		Log.d("tags", "textview   : "+Html.toHtml(textInput.getText()));
		Log.d("tags", "textbefore : "+beforeText);
		Log.e("tags", "title      : "+titleView.getText().toString().equals(beforeTitle));
		Log.e("tags", "text       : "+Html.toHtml(textInput.getText()).equals(beforeText));
		if(titleView.getText().toString().equals(beforeTitle)&&Html.toHtml(textInput.getText()).equals(Html.toHtml(Html.fromHtml(beforeText)))){
			finish();
			return;
		}
		else{
			final ConstraintLayout layout = (ConstraintLayout) View.inflate(NoteEditActivity.this, R.layout.dialog_doublecheck, null);
			new AlertDialog.Builder(NoteEditActivity.this)
					.setView(layout)
					.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();
		}
	}


	public static Spannable merge(String recent, String before, String recentTitle, String beforeTitle, boolean isTitle){

		int recentLines, beforeLines, i, j, m=0, t=0;
		int[][] score = new int[1500][1500];
		int[][] pos = new int[1500][1500];
		CharSequence[] recentSequences = new CharSequence[1500];
		CharSequence[] beforeSequences = new CharSequence[1500];

		SpannableStringBuilder builder = new SpannableStringBuilder();

		SpannableStringBuilder recentBuilder = new SpannableStringBuilder(Html.fromHtml(recent)).append("\n");
		SpannableStringBuilder beforeBuilder = new SpannableStringBuilder(Html.fromHtml(before)).append("\n");

		m = 0;
		t = 0;
		for(i=0; i<recentBuilder.length(); i++){
			if(recentBuilder.charAt(i)=='\n'){
				recentSequences[t] = recentBuilder.subSequence(m, i);
				t++;
				Log.e("TAG", "merge: recent, "+m+"~"+i+"/"+t);
				m = i+1;
			}
		}
		recentLines = t;
		m = 0;
		t = 0;
		for(i=0; i<beforeBuilder.length(); i++){
			if(beforeBuilder.charAt(i)=='\n'){
				beforeSequences[t] = beforeBuilder.subSequence(m, i);
				t++;
				Log.e("TAG", "merge: before, "+m+"~"+i+"/"+t);
				m = i+1;
			}
		}
		beforeLines = t;

		score[0][0] = 1;
		for(i=0; i<recentLines; i++){
			for(j=0; j<beforeLines; j++){
				int count = lineup(recentSequences[i], beforeSequences[j]);
				if(compare(recentSequences[i], beforeSequences[j])){
					if(score[i][j]>=score[i+1][j+1]){
						if(recentSequences[i].length()>2){
							score[i+1][j+1] = score[i][j]+recentSequences[i].length()*2;
						}
						else{
							score[i+1][j+1] = score[i][j]+2;
						}
						pos[i+1][j+1] = 3;
						Log.d("TAG", "merge: "+i+" "+j);
					}
				}
				if(score[i][j]>score[i][j+1]){
					score[i][j+1] = score[i][j];
					pos[i][j+1] = 2;
				}
				if(score[i][j]>score[i+1][j]){
					score[i+1][j] = score[i][j];
					pos[i+1][j] = 1;
				}
				if(score[i][j]+count>score[i+1][j+1]){
					score[i+1][j+1] = score[i][j] + count;
					pos[i+1][j+1] = 4;
				}
			}
		}


		i = recentLines;
		j = beforeLines;
		Log.e("TAG", "merge: "+i+" "+j+" "+pos[i][j]);
		pos[i][j] = 3;
		while(i>0||j>0){
			if(pos[i][j]==1){
				i--;
				SpannableStringBuilder instant = new SpannableStringBuilder(recentSequences[i]);
				instant.setSpan(new BackgroundColorSpan(0xff88ff88), 0, instant.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				builder.insert(0, "\n");
				builder.insert(0, instant);
				Log.d("TAG", "merge: "+i+" "+j);
			}
			if(pos[i][j]==2){
				j--;
				SpannableStringBuilder instant = new SpannableStringBuilder(beforeSequences[j]);
				instant.setSpan(new BackgroundColorSpan(0xffff8888), 0, instant.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				builder.insert(0, "\n");
				builder.insert(0, instant);
				Log.d("TAG", "merge: "+i+" "+j);
			}
			if(pos[i][j]==3){
				i--;
				j--;
				builder.insert(0, "\n");
				builder.insert(0, recentSequences[i]);
				Log.d("TAG", "merge: "+i+" "+j);
			}
			if(pos[i][j]==4){
				i--;
				j--;
				SpannableStringBuilder instant = new SpannableStringBuilder(recentSequences[i]);
				instant.setSpan(new BackgroundColorSpan(0xff88ff88), 0, instant.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				builder.insert(0, "\n");
				builder.insert(0, instant);
				instant = new SpannableStringBuilder(beforeSequences[j]);
				instant.setSpan(new BackgroundColorSpan(0xffff8888), 0, instant.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				builder.insert(0, "\n");
				builder.insert(0, instant);
			}
		}

		if(isTitle&&!beforeTitle.equals(recentTitle)){
			SpannableStringBuilder instant = new SpannableStringBuilder("title from \""+beforeTitle+"\"\n");
			instant.setSpan(new BackgroundColorSpan(0xffff8888), 0, instant.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			builder.insert(0, instant);
		}
		if(!isTitle&&!beforeTitle.equals(recentTitle)){
			SpannableStringBuilder instant = new SpannableStringBuilder("title is now \""+recentTitle+"\"\n");
			instant.setSpan(new BackgroundColorSpan(0xff88ff88), 0, instant.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			builder.insert(0, instant);
		}
		return builder;
	}

	static boolean compare(CharSequence a, CharSequence b){
		if(!a.toString().equals(b.toString())) return false;

		Log.d("TAG", "compare: a, \""+a.toString()+"\"");
		Log.d("TAG", "compare: b, \""+b.toString()+"\"");

		final Spannable spanA = new SpannableStringBuilder(a);
		StyleSpan[] styleSpansA = spanA.getSpans(0, spanA.length(), StyleSpan.class);
		UnderlineSpan[] underlineSpansA = spanA.getSpans(0, spanA.length(), UnderlineSpan.class);
		StrikethroughSpan[] strikethroughSpansA = spanA.getSpans(0, spanA.length(), StrikethroughSpan.class);
		ForegroundColorSpan[] foregroundColorSpansA = spanA.getSpans(0, spanA.length(), ForegroundColorSpan.class);

		final Spannable spanB = new SpannableStringBuilder(b);
		StyleSpan[] styleSpansB = spanB.getSpans(0, spanB.length(), StyleSpan.class);
		UnderlineSpan[] underlineSpansB = spanB.getSpans(0, spanB.length(), UnderlineSpan.class);
		StrikethroughSpan[] strikethroughSpansB = spanB.getSpans(0, spanB.length(), StrikethroughSpan.class);
		ForegroundColorSpan[] foregroundColorSpansB = spanB.getSpans(0, spanB.length(), ForegroundColorSpan.class);

		ArrayList<StyleSpan> sa1 = new ArrayList<>();
		ArrayList<StyleSpan> sa2 = new ArrayList<>();
		ArrayList<StyleSpan> sb1 = new ArrayList<>();
		ArrayList<StyleSpan> sb2 = new ArrayList<>();

		Arrays.sort(styleSpansA, new Comparator<StyleSpan>() {
			@Override
			public int compare(StyleSpan o1, StyleSpan o2) {
				if(o1.getStyle()<o2.getStyle()){
					return 1;
				}
				if((o1.getStyle()==o2.getStyle())&&(spanA.getSpanStart(o1)<spanA.getSpanStart(o2))) {
					return 1;
				}
				return 0;
			}
		});
		Arrays.sort(styleSpansB, new Comparator<StyleSpan>() {
			@Override
			public int compare(StyleSpan o1, StyleSpan o2) {
				if(o1.getStyle()<o2.getStyle()){
					return 1;
				}
				if((o1.getStyle()==o2.getStyle())&&(spanB.getSpanStart(o1)<spanB.getSpanStart(o2))){
					return 1;
				}
				return 0;
			}
		});
		for(int i=0; i<styleSpansA.length; i++){
			if(styleSpansA[i].getStyle()==1){
				sa1.add(styleSpansA[i]);
			}
			else{
				sa2.add(styleSpansA[i]);
			}
		}
		for(int i=0; i<styleSpansB.length; i++){
			if(styleSpansB[i].getStyle()==1){
				sb1.add(styleSpansB[i]);
			}
			else{
				sb2.add(styleSpansB[i]);
			}
		}


		Arrays.sort(underlineSpansA, new Comparator<UnderlineSpan>() {
			@Override
			public int compare(UnderlineSpan o1, UnderlineSpan o2) {
				if(spanA.getSpanStart(o1)<spanA.getSpanStart(o2)){
					return 1;
				}
				if(spanA.getSpanStart(o1)==spanA.getSpanStart(o2)){
					return 1;
				}
				return 0;
			}
		});
		Arrays.sort(underlineSpansB, new Comparator<UnderlineSpan>() {
			@Override
			public int compare(UnderlineSpan o1, UnderlineSpan o2) {
				if(spanB.getSpanStart(o1)<spanB.getSpanStart(o2)){
					return 1;
				}
				if(spanB.getSpanStart(o1)==spanB.getSpanStart(o2)){
					return 1;
				}
				return 0;
			}
		});
		Arrays.sort(strikethroughSpansA, new Comparator<StrikethroughSpan>() {
			@Override
			public int compare(StrikethroughSpan o1, StrikethroughSpan o2) {
				if(spanA.getSpanStart(o1)<spanA.getSpanStart(o2)){
					return 1;
				}
				if((spanA.getSpanStart(o1)==spanA.getSpanStart(o2))){
					return 1;
				}
				return 0;
			}
		});
		Arrays.sort(strikethroughSpansB, new Comparator<StrikethroughSpan>() {
			@Override
			public int compare(StrikethroughSpan o1, StrikethroughSpan o2) {
				if(spanB.getSpanStart(o1)<spanB.getSpanStart(o2)){
					return 1;
				}
				if((spanB.getSpanStart(o1)==spanB.getSpanStart(o2))){
					return 1;
				}
				return 0;
			}
		});
		Arrays.sort(foregroundColorSpansA, new Comparator<ForegroundColorSpan>() {
			@Override
			public int compare(ForegroundColorSpan o1, ForegroundColorSpan o2) {
				if(spanA.getSpanStart(o1)<spanA.getSpanStart(o2)){
					return 1;
				}
				if((spanA.getSpanStart(o1)==spanA.getSpanStart(o2))){
					return 1;
				}
				return 0;
			}
		});
		Arrays.sort(foregroundColorSpansB, new Comparator<ForegroundColorSpan>() {
			@Override
			public int compare(ForegroundColorSpan o1, ForegroundColorSpan o2) {
				if(spanB.getSpanStart(o1)<spanB.getSpanStart(o2)){
					return 1;
				}
				if((spanB.getSpanStart(o1)==spanB.getSpanStart(o2))){
					return 1;
				}
				return 0;
			}
		});

		Log.i("TAG", "compare: "+styleSpansA.length+"/"+styleSpansB.length);
		Log.i("TAG", "compare: "+underlineSpansA.length+"/"+underlineSpansB.length);
		Log.i("TAG", "compare: "+strikethroughSpansA.length+"/"+strikethroughSpansB.length);
		Log.i("TAG", "compare: "+foregroundColorSpansA.length+"/"+foregroundColorSpansB.length);

		if(styleSpansA.length!=styleSpansB.length) return false;
		if(sa1.size()!=sb1.size()) return false;
		if(sa2.size()!=sb2.size()) return false;
		if(underlineSpansA.length!=underlineSpansB.length) return false;
		if(strikethroughSpansA.length!=strikethroughSpansB.length) return false;
		if(foregroundColorSpansA.length!=foregroundColorSpansB.length) return false;

		for(int i=0; i<sa1.size(); i++){
			if(spanA.getSpanStart(sa1.get(i))!=spanB.getSpanStart(sb1.get(i))) return false;
			if(spanA.getSpanEnd(sa1.get(i))!=spanB.getSpanEnd(sb1.get(i))) return false;
		}
		for(int i=0; i<sa2.size(); i++){
			if(spanA.getSpanStart(sa2.get(i))!=spanB.getSpanStart(sb2.get(i))) return false;
			if(spanA.getSpanEnd(sa2.get(i))!=spanB.getSpanEnd(sb2.get(i))) return false;
		}

		for(int i=0; i<underlineSpansA.length; i++){
			Log.i("TAG", "compare, 2 start : "+spanA.getSpanStart(underlineSpansA[i])+"/"+spanB.getSpanStart(underlineSpansB[i]));
			if(spanA.getSpanStart(underlineSpansA[i])!=spanB.getSpanStart(underlineSpansB[i])) return false;
			Log.i("TAG", "compare, 2 end   : "+spanA.getSpanEnd(underlineSpansA[i])+"/"+spanB.getSpanEnd(underlineSpansB[i]));
			if(spanA.getSpanEnd(underlineSpansA[i])!=spanB.getSpanEnd(underlineSpansB[i])) return false;
		}
		for(int i=0; i<strikethroughSpansA.length; i++){
			Log.i("TAG", "compare, 2 start : "+spanA.getSpanEnd(strikethroughSpansA[i])+"/"+spanB.getSpanEnd(strikethroughSpansB[i]));
			if(spanA.getSpanStart(strikethroughSpansA[i])!=spanB.getSpanStart(strikethroughSpansB[i])) return false;
			Log.i("TAG", "compare, 2 end   : "+spanA.getSpanEnd(strikethroughSpansA[i])+"/"+spanB.getSpanEnd(strikethroughSpansB[i]));
			if(spanA.getSpanEnd(strikethroughSpansA[i])!=spanB.getSpanEnd(strikethroughSpansB[i])) return false;
		}
		for(int i=0; i<foregroundColorSpansA.length; i++){
			if(spanA.getSpanStart(foregroundColorSpansA[i])!=spanB.getSpanStart(foregroundColorSpansB[i])) return false;
			if(spanA.getSpanEnd(foregroundColorSpansA[i])!=spanB.getSpanEnd(foregroundColorSpansB[i])) return false;
			if(foregroundColorSpansA[i].getForegroundColor()!=foregroundColorSpansB[i].getForegroundColor()) return false;
		}
		return true;
	}

	static int lineup(CharSequence a, CharSequence b){
		if(a.length()<1||b.length()<1) {
			return 0;
		}

		char[] as = a.toString().toCharArray();
		char[] bs = b.toString().toCharArray();

		int c = 0;

		for(int i=0; i<a.length()&&i<1000; i++){
			for(int j=0; j<b.length()&&j<1000; j++){
				if(as[i]==bs[j]){
					c++;
					break;
				}
			}
		}
		for(int j=0; j<b.length()&&j<1000; j++){
			for(int i=0; i<a.length()&&i<1000; i++){
				if(bs[j]==as[i]){
					c++;
					break;
				}
			}
		}
		if(c*2<as.length+bs.length) return 0;
		return c/2;
	}
}
