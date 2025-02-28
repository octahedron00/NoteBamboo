package com.octahedron00.notebamboo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

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

public class SettingsListActivity extends AppCompatActivity {

	int list, level, no, owner;
	String title;
	byte[] AES_key;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_list);

		Intent intent = getIntent();
		level = intent.getIntExtra("level",0);
		list = intent.getIntExtra("list",0);
		title = intent.getStringExtra("title");
		AES_key = intent.getByteArrayExtra("AES_key");
		owner = intent.getIntExtra("owner", 0);
		no = intent.getIntExtra("no",0);

		TextView deleteText = findViewById(R.id.deleteText);
		if(level<5){
			deleteText.setText("Delete my sharing");
		}
		setResult(100);
	}

	public void share(View view){
		Intent intent = new Intent(this, ShareActivity.class);
		intent.putExtra("list",list);
		intent.putExtra("level",level);
		intent.putExtra("title",title);
		intent.putExtra("AES_key",AES_key);
		intent.putExtra("owner",owner);
		startActivity(intent);
		finish();
	}

	public void setTitle(View view){
		final LinearLayout layout = (LinearLayout) View.inflate(SettingsListActivity.this,R.layout.dialog_listadd, null);
		TextView textView = layout.findViewById(R.id.dialogTextView);
		textView.setText("Insert the title\n(change on you only)");
		new AlertDialog.Builder(SettingsListActivity.this)
				.setView(layout)
				.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText inputTitle = layout.findViewById(R.id.inputTitle);
						String title = inputTitle.getText().toString();
						if(title.length()<1){
							Toast.makeText(SettingsListActivity.this, "Input the title", Toast.LENGTH_SHORT).show();
						}
						else if(title.length()>20){
							Toast.makeText(SettingsListActivity.this, "Title must be shorter than 20 words", Toast.LENGTH_SHORT).show();
						}
						else{
							SecretKey key = new SecretKeySpec(Data.user_key, "AES");
							try {
								Cipher sha;
								sha = Cipher.getInstance("AES/CBC/PKCS5PADDING");
								sha.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(Data.iv));
								String name_Enc = Base64.encodeToString(sha.doFinal(title.getBytes()), Base64.NO_WRAP);
								Response.Listener<String> listener = new Response.Listener<String>() {
									@Override
									public void onResponse(String response) {
										Log.e("error?", "onResponse: "+response);
										Toast.makeText(SettingsListActivity.this, "Change Complete", Toast.LENGTH_SHORT).show();
									}
								};
								Map<String, String> addMap = new HashMap<>();
								addMap.put("method", "list_name");
								addMap.put("name_Enc", name_Enc);
								addMap.put("list", Integer.toString(list));
								addMap.put("user", Integer.toString(Data.user_no));
								AllRequest addRequest = new AllRequest(addMap, listener);
								RequestQueue queue = Volley.newRequestQueue(SettingsListActivity.this);
								queue.add(addRequest);
							} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
								e.printStackTrace();
							}
							dialog.dismiss();
						}
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
		setResult(101);
	}

	public void delete(View view){
		final ConstraintLayout layout = (ConstraintLayout) View.inflate(SettingsListActivity.this, R.layout.dialog_delete, null);
		new AlertDialog.Builder(SettingsListActivity.this)
				.setView(layout)
				.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Response.Listener<String> listener = new Response.Listener<String>() {
							@Override
							public void onResponse(String response) {
								Toast.makeText(getApplicationContext(),"List deleted",Toast.LENGTH_SHORT).show();
								setResult(101);
								finish();
							}
						};
						Map<String, String> map = new HashMap<>();
						RequestQueue queue = Volley.newRequestQueue(SettingsListActivity.this);
						if(level<5){
							map.put("method", "sharing_delete");
							map.put("user", Integer.toString(Data.user_no));
							map.put("no", Integer.toString(no));
						}
						else{
							map.put("method", "list_hide");
							map.put("list", Integer.toString(list));
						}
						AllRequest request = new AllRequest(map, listener);
						queue.add(request);

					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}
}