package com.octahedron00.notebamboo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class NoteReadActivity extends AppCompatActivity {

	byte[] AES_key;
	int note, level, add, viewCount=0, recentLength=0;
	boolean editable;
	String recentText, recentTitle, beforeText, beforeTitle;

	ImageView imageBack, imageEdit, imageTrash;
	TextView titleView, textView, textEdited;
	ScrollView noteBack;

	DBHelper helper;
	SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_read);

		Intent intent = getIntent();
		add = intent.getIntExtra("add",0);
		note = intent.getIntExtra("note",0);
		level = intent.getIntExtra("level", 0);
		editable = intent.getBooleanExtra("editable", true);
		AES_key = intent.getByteArrayExtra("AES_key");

		titleView = findViewById(R.id.titleView);
		textView = findViewById(R.id.textView);
		textEdited = findViewById(R.id.textEdited);
		imageBack = findViewById(R.id.imageBack);
		imageEdit = findViewById(R.id.imageEdit);
		imageTrash = findViewById(R.id.imageTrash);
		noteBack = findViewById(R.id.noteBackground);

		if(level<3){
			imageTrash.setVisibility(View.GONE);
		}
		if(level<2){
			imageEdit.setVisibility(View.GONE);
		}

		helper = new DBHelper(this);

		setResult(100);

		if(editable){
			imageEdit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					edit();
				}
			});
			imageTrash.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					delete();
				}
			});
		}
		else{
			imageEdit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					merge();
				}
			});
			imageTrash.setVisibility(View.GONE);
		}
		imageBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});


		if(intent.getBooleanExtra("next",false)){
			edit();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if(editable){
			refresh();
		}
		else{
			set();
		}
	}

	public void edit(){
		Intent intent = new Intent(NoteReadActivity.this, NoteEditActivity.class);
		intent.putExtra("AES_key",AES_key);
		intent.putExtra("note",note);
		intent.putExtra("level",level);
		startActivity(intent);
	}

	public void merge(){
		setResult(101);
		final ConstraintLayout layout = (ConstraintLayout) View.inflate(NoteReadActivity.this, R.layout.dialog_mergecheck, null);
		new AlertDialog.Builder(NoteReadActivity.this)
				.setView(layout)
				.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						try{
							Intent intent = getIntent();

							SecretKey key = new SecretKeySpec(AES_key, "AES");
							Cipher decode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
							decode.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(Data.iv_def));

							final String title = new String(decode.doFinal(Base64.decode(intent.getStringExtra("title"), Base64.NO_WRAP)));
							final String text = new String(decode.doFinal(Base64.decode(intent.getStringExtra("text"), Base64.NO_WRAP)));

							intent = new Intent(NoteReadActivity.this, NoteEditActivity.class);
							intent.putExtra("AES_key", AES_key);
							intent.putExtra("note", note);
							intent.putExtra("add", add);
							intent.putExtra("level", level);
							intent.putExtra("baseTitle", title);
							intent.putExtra("baseText", text);
							intent.putExtra("merge", true);
							startActivity(intent);

						} catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | InvalidKeyException e) {
							e.printStackTrace();
						}
						finish();
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();

	}


	public void refresh(){
		final Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					db = helper.getWritableDatabase();

					JSONObject object = new JSONObject(response);
					Log.d("Response", "onResponse: "+response);
					if(!object.getBoolean("success")){
						Toast.makeText(NoteReadActivity.this, "Something Wrong!", Toast.LENGTH_SHORT).show();
						finish();
					}

					SecretKey key = new SecretKeySpec(AES_key, "AES");
					Cipher decode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
					decode.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(Data.iv_def));

					JSONArray array = object.getJSONArray("array");

					String title="", text="";
					final String finalText, finalTitle;
					final int finalLength;
					int length = 0;
					JSONObject next = array.getJSONObject(0);
					for(int i=0; i<array.length(); i++){

						if(next.getInt("child")>0){
							beforeText = text;
							beforeTitle = title;
						}

						next = array.getJSONObject(i);

						title = new String(decode.doFinal(Base64.decode(next.getString("title"),Base64.NO_WRAP)));
						text = new String(decode.doFinal(Base64.decode(next.getString("text"), Base64.NO_WRAP)));
						String titleEnc = next.getString("title");
						String textEnc = next.getString("text");

						if(length<next.getInt("length")){
							length = next.getInt("length");
						}

						String sql = "delete from version where version="+next.getInt("no")+";";
						db.execSQL(sql);
						sql = "insert into version (version, note, user, id, title, text, time, length) values (" +
								next.getInt("no")+", " +
								next.getInt("note")+", " +
								next.getInt("user")+", '" +
								next.getString("id")+"', '" +
								titleEnc+"', '" +
								textEnc+"', '" +
								next.getString("time")+"', " +
								next.getInt("length")+");";
						db.execSQL(sql);
						Log.d("TAG", "onResponse: "+sql);
					}

					db.execSQL("delete from notes where note="+note+";");
					db.execSQL("insert into notes (note, length) values ("+note+", "+length+")");

					finalText = text;
					finalTitle = title;
					finalLength = length;
					db.close();
					titleView.setText(title);
					textView.setText(Html.fromHtml(finalText));

					final String metadata = "by "+next.getString("id")+", "+next.getString("time");
					textEdited.setText(metadata);

					noteBack.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							textEdited.setText(metadata);
							textView.setText(Html.fromHtml(finalText));
							viewCount = 0;
						}
					});
					titleView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(viewCount>0){
								textEdited.setText(metadata);
								textView.setText(Html.fromHtml(finalText));
								viewCount = 0;
							}
							else{
								textEdited.setText("HTML view of "+finalTitle);
								textView.setText(finalText);
								viewCount = 1;
							}
						}
					});
					textView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(viewCount>0){
								textEdited.setText(metadata);
								textView.setText(Html.fromHtml(finalText));
								viewCount = 0;
							}
							else{
								if(finalLength<1){
									textEdited.setText("version null > this("+finalLength+")");
									textView.setText(NoteEditActivity.merge(finalText, "<br><br>", finalTitle, finalTitle, false));
								}
								else{
									textEdited.setText("version parent("+(finalLength-1)+") > this("+finalLength+")");
									textView.setText(NoteEditActivity.merge(finalText, beforeText, finalTitle, beforeTitle, true));
								}
								viewCount = 2;
							}
						}
					});
					textEdited.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Log.d("as", "onClick, NoteRead -> Version: ");
							Intent intent = new Intent(NoteReadActivity.this, VersionActivity.class);
							intent.putExtra("AES_key",AES_key);
							intent.putExtra("note",note);
							intent.putExtra("level",level);
							startActivity(intent);
						}
					});

					Response.Listener<String> listener = new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							try {
								JSONObject object = new JSONObject(response);
								JSONArray array = object.getJSONArray("array");
								if(array.length()>1){
									SpannableStringBuilder builder = new SpannableStringBuilder(textEdited.getText());
									builder.setSpan(new ForegroundColorSpan(0xFFFF0000), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
									builder.setSpan(new UnderlineSpan(), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
									textEdited.setText(builder);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					};
					Map<String, String> map = new HashMap<>();
					map.put("method", "note_ends");
					map.put("note", Integer.toString(note));
					final AllRequest request = new AllRequest(map, listener);
					RequestQueue queue = Volley.newRequestQueue(NoteReadActivity.this);
					queue.add(request);


				} catch (JSONException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
					e.printStackTrace();
					finish();
				}
			}
		};
		Map<String, String> map = new HashMap<>();
		map.put("method", "note_read");
		map.put("note", Integer.toString(note));
		AllRequest request = new AllRequest(map, listener);
		RequestQueue queue = Volley.newRequestQueue(this);
		queue.add(request);
	}

	public void delete(){
		final ConstraintLayout layout = (ConstraintLayout) View.inflate(NoteReadActivity.this, R.layout.dialog_delete, null);
		new AlertDialog.Builder(NoteReadActivity.this)
				.setView(layout)
				.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Response.Listener<String> listener = new Response.Listener<String>() {
							@Override
							public void onResponse(String response) {
								Toast.makeText(NoteReadActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
								finish();
							}
						};
						Map<String, String> map = new HashMap<>();
						map.put("method", "note_hide");
						map.put("note", Integer.toString(note));
						AllRequest request = new AllRequest(map, listener);
						RequestQueue queue = Volley.newRequestQueue(NoteReadActivity.this);
						queue.add(request);
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}

	void set(){
		try {

			SecretKey key = new SecretKeySpec(AES_key, "AES");
			Cipher decode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			decode.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(Data.iv_def));

			Intent intent = getIntent();

			final String metadata = intent.getStringExtra("metadata");
			final String title = new String(decode.doFinal(Base64.decode(intent.getStringExtra("title"),Base64.NO_WRAP)));
			final String text = new String(decode.doFinal(Base64.decode(intent.getStringExtra("text"), Base64.NO_WRAP)));
			final boolean end = intent.getBooleanExtra("end", true);
			final String titlePa, textPa;
			if(end){
				titlePa = "";
				textPa = "";
			}
			else{
				titlePa = new String(decode.doFinal(Base64.decode(intent.getStringExtra("title_origin"),Base64.NO_WRAP)));
				textPa = new String(decode.doFinal(Base64.decode(intent.getStringExtra("text_origin"), Base64.NO_WRAP)));
			}

			final int length = intent.getIntExtra("length", 0);
			final int lengthPa = intent.getIntExtra("length_origin", 0);

			noteBack.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					textEdited.setText(metadata);
					textView.setText(Html.fromHtml(text));
					viewCount = 0;
				}
			});
			titleView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(viewCount>0){
						textEdited.setText(metadata);
						textView.setText(Html.fromHtml(text));
						viewCount = 0;
					}
					else{
						textEdited.setText("HTML view of "+title);
						textView.setText(text);
						viewCount = 1;
					}
				}
			});
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(viewCount>0){
						textEdited.setText(metadata);
						textView.setText(Html.fromHtml(text));
						viewCount = 0;
					}
					else{
						if(end){
							textEdited.setText("version null > this("+length+")");
							textView.setText(NoteEditActivity.merge(text, "", title, title, false));
						}
						else{
							textEdited.setText("version parent("+lengthPa+") > this("+length+")");
							textView.setText(NoteEditActivity.merge(text, textPa, title, titlePa, true));
						}
						viewCount = 2;
					}
				}
			});
			textEdited.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(viewCount>0){
						textEdited.setText(metadata);
						textView.setText(Html.fromHtml(text));
						viewCount = 0;
					}
					else{
						textEdited.setText("version this("+length+") > recent("+recentLength+")");
						textView.setText(NoteEditActivity.merge(recentText, text, recentTitle, title, false));
						viewCount = 3;
					}
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
						recentTitle = new String(decode.doFinal(Base64.decode(object.getString("title"),Base64.NO_WRAP)));
						recentText = new String(decode.doFinal(Base64.decode(object.getString("text"), Base64.NO_WRAP)));
						recentLength = object.getInt("length");

						titleView.setText(title);
						textView.setText(Html.fromHtml(text));
						textEdited.setText(metadata);

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

		} catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
			e.printStackTrace();
			finish();
		}
	}
}