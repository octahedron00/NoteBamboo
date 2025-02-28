package com.octahedron00.notebamboo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class VersionActivity extends AppCompatActivity {

	byte[] AES_key;
	int note, level;

	TextView VersionTitle;
	LinearLayout VersionContainerTop, VersionContainerBottom;

	DBHelper helper;
	SQLiteDatabase db;

	String endText, endTitle, pureTitle, nowTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_version);

		Intent intent = getIntent();
		note = intent.getIntExtra("note",0);
		level = intent.getIntExtra("level", 0);
		AES_key = intent.getByteArrayExtra("AES_key");

		VersionTitle = findViewById(R.id.VersionTitle);
		VersionContainerTop = findViewById(R.id.VersionContainerTop);
		VersionContainerBottom = findViewById(R.id.VersionContainerBottom);

		helper = new DBHelper(this);


	}

	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	void refresh(){
		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				VersionContainerTop.removeAllViews();

				ArrayList<Integer> ends = new ArrayList<>();

				Log.i("TAG", "onResponse, note_ends: "+response);
				try {
					JSONObject object = new JSONObject(response);
					if(!object.getBoolean("success")){
						Toast.makeText(VersionActivity.this, "Server Connection Went Wrong!", Toast.LENGTH_SHORT).show();
						finish();
					}
					SecretKey key = new SecretKeySpec(AES_key, "AES");
					Cipher decode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
					decode.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(Data.iv_def));

					JSONArray array = object.getJSONArray("array");

					JSONObject next;
					for(int i=0; i<array.length(); i++){
						next = array.getJSONObject(i);

						final int no = next.getInt("no");
						ends.add(no);
						final String title = next.getString("title");
						final String text = next.getString("text");
						pureTitle = new String(decode.doFinal(Base64.decode(next.getString("title"),Base64.NO_WRAP)));
						final String metadata = "by "+next.getString("id")+", "+next.getString("time");

						View view = MainActivity.inflater.inflate(R.layout.activity_version_object, null);

						TextView lengthView = view.findViewById(R.id.lengthView);
						TextView userView = view.findViewById(R.id.userView);
						TextView timeView = view.findViewById(R.id.timeView);
						ImageView editOnView = view.findViewById(R.id.editOnView);
						ImageView deleteOnView = view.findViewById(R.id.deleteOnView);
						ConstraintLayout VersionBackground = view.findViewById(R.id.VersionBackground);

						Log.d("next", "Row: "+next.toString());

						lengthView.setText(Integer.toString(next.getInt("length")));
						userView.setText("by "+next.getString("id"));
						timeView.setText("at UTC "+next.getString("time"));

						deleteOnView.setVisibility(View.GONE);

						if(i==0){

							VersionBackground.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									finish();
								}
							});
							editOnView.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									if(level>1) {
										Intent share = new Intent(VersionActivity.this, NoteEditActivity.class);
										share.putExtra("note", note);
										share.putExtra("level", level);
										share.putExtra("AES_key", AES_key);
										startActivity(share);
										finish();
									}
									else{
										Toast.makeText(VersionActivity.this, "Level is too low", Toast.LENGTH_SHORT).show();
									}
								}
							});
							endText = text;
							endTitle = title;
							nowTitle = pureTitle;
							VersionTitle.setText("Version list of\n"+nowTitle);
						}
						else{
							VersionBackground.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(VersionActivity.this, NoteReadActivity.class);
									intent.putExtra("AES_key",AES_key);
									intent.putExtra("note",note);
									intent.putExtra("level",level);
									intent.putExtra("editable",false);
									intent.putExtra("add",no);
									intent.putExtra("title", title);
									intent.putExtra("text", text);
									intent.putExtra("metadata", metadata);
									startActivity(intent);
								}
							});
							VersionBackground.setBackgroundColor(0xFFFFCCCC);
							editOnView.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									if(level>1) {
										try {
											SecretKey key = new SecretKeySpec(AES_key, "AES");
											final Cipher decode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
											decode.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(Data.iv_def));


											final ConstraintLayout layout = (ConstraintLayout) View.inflate(VersionActivity.this, R.layout.dialog_mergecheck, null);
											new AlertDialog.Builder(VersionActivity.this)
													.setView(layout)
													.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog, int which) {
															dialog.dismiss();

															try{
																Intent intent = new Intent(VersionActivity.this, NoteEditActivity.class);
																intent.putExtra("AES_key", AES_key);
																intent.putExtra("note", note);
																intent.putExtra("add", no);
																intent.putExtra("level", level);
																intent.putExtra("baseTitle", new String(decode.doFinal(Base64.decode(title,Base64.NO_WRAP))));
																intent.putExtra("baseText", new String(decode.doFinal(Base64.decode(text,Base64.NO_WRAP))));
																intent.putExtra("merge", true);
																startActivity(intent);

															} catch (BadPaddingException | IllegalBlockSizeException e) {
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

										} catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
											e.printStackTrace();
										}
									}
									else{
										Toast.makeText(VersionActivity.this, "Level is too low", Toast.LENGTH_SHORT).show();
									}
								}
							});
						}
						VersionContainerTop.addView(view);

					}

					db = helper.getWritableDatabase();

					for(int i=0; i<ends.size(); i++){
						db.execSQL("delete from version where version="+ends.get(i)+";");
					}
					db.close();

					db = helper.getReadableDatabase();

					Cursor cursor = db.rawQuery("select version, id, title, text, time, length, note from version where note="+note+" order by length desc, version asc;", null);
					int l = cursor.getCount();
					Log.e("TAG", "Cursor.getcount : "+l);
					int limit=0;
					int[] number = new int[65536];

					VersionContainerBottom.removeAllViews();

					for(int i=0; i<l; i++) {
						cursor.moveToPosition(i);
						int length = cursor.getInt(5);
						if(limit<=length) limit = length+1;
						number[length]++;
						Log.e("TAG", "Cursor.getlength : "+length);
					}



					for(int i=0; i<l; i++){
						cursor.moveToPosition(i);
						final int length = cursor.getInt(5);
						final int note = cursor.getInt(6);
						final int version = cursor.getInt(0);
						final String id = cursor.getString(1);
						final String time = cursor.getString(4);
						final String text = cursor.getString(3);
						final String title = cursor.getString(2);
						Log.d("TAG", "refresh: "+i+"=>"+length+"/"+title+"/"+id+"/"+time+"////"+version+" of "+note);

						final String metadata = "by "+id+", "+time;

						if(limit-length>1){
							View gap = MainActivity.inflater.inflate(R.layout.activity_versionempty_object, null);
							final TextView lengthStartView = gap.findViewById(R.id.lengthStartView);
							final TextView lengthEndView = gap.findViewById(R.id.lengthEndView);
							lengthStartView.setText(Integer.toString(length+1));
							lengthEndView.setText(Integer.toString(limit-1));
							VersionContainerBottom.addView(gap);
						}
						limit = length;

						View view = MainActivity.inflater.inflate(R.layout.activity_version_object, null);

						final TextView lengthView = view.findViewById(R.id.lengthView);
						final TextView userView = view.findViewById(R.id.userView);
						final TextView timeView = view.findViewById(R.id.timeView);
						final ImageView editOnView = view.findViewById(R.id.editOnView);
						final ImageView deleteOnView = view.findViewById(R.id.deleteOnView);
						final LinearLayout ChildContainer = view.findViewById(R.id.ChildContainer);
						final ConstraintLayout VersionBackground = view.findViewById(R.id.VersionBackground);

						lengthView.setText(Integer.toString(length));
						userView.setText("by "+id);
						timeView.setText("at UTC "+time);

						deleteOnView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {final ConstraintLayout layout = (ConstraintLayout) View.inflate(VersionActivity.this, R.layout.dialog_delete, null);
								new AlertDialog.Builder(VersionActivity.this)
										.setView(layout)
										.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
												Toast.makeText(VersionActivity.this, "Version deleted", Toast.LENGTH_SHORT).show();
												SQLiteDatabase database = helper.getWritableDatabase();
												database.execSQL("delete from version where note="+note+" and length="+length+";");
												database.close();
												refresh();
											}
										}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								}).show();
							}
						});
						editOnView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if(level>1) {
									try {
										SecretKey key = new SecretKeySpec(AES_key, "AES");
										final Cipher decode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
										decode.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(Data.iv_def));

										final ConstraintLayout layout = (ConstraintLayout) View.inflate(VersionActivity.this, R.layout.dialog_mergecheck, null);
										new AlertDialog.Builder(VersionActivity.this)
												.setView(layout)
												.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														dialog.dismiss();

														try{
															Intent intent = new Intent(VersionActivity.this, NoteEditActivity.class);
															intent.putExtra("AES_key", AES_key);
															intent.putExtra("note", note);
															intent.putExtra("add", version);
															intent.putExtra("level", level);
															intent.putExtra("baseTitle", new String(decode.doFinal(Base64.decode(title,Base64.NO_WRAP))));
															intent.putExtra("baseText", new String(decode.doFinal(Base64.decode(text,Base64.NO_WRAP))));
															intent.putExtra("merge", true);
															startActivity(intent);
															finish();

														} catch (BadPaddingException | IllegalBlockSizeException e) {
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
									} catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
										e.printStackTrace();
										finish();
									}
								}
								else{
									Toast.makeText(VersionActivity.this, "Level is too low", Toast.LENGTH_SHORT).show();
								}
							}
						});
						VersionBackground.setOnClickListener(new View.OnClickListener() {
							boolean isOpen = false;
							@Override
							public void onClick(View v) {
								if(isOpen){
									ChildContainer.setVisibility(View.GONE);
									isOpen = false;
									Log.e("TAG", "onClick: true -> false");
								}
								else{
									ChildContainer.setVisibility(View.VISIBLE);
									isOpen = true;
									Log.e("TAG", "onClick: false -> true");
								}
							}
						});

						if(number[length]>1){
							userView.setText("by multiple users");
							timeView.setText("at multiple times");
							editOnView.setVisibility(View.GONE);


							for(int j=0; j<number[length]; j++){

								cursor.moveToPosition(i+j);
								final int noCh = cursor.getInt(0);
								final String idCh = cursor.getString(1);
								final String timeCh = cursor.getString(4);
								final String titleCh = cursor.getString(2);
								final String textCh = cursor.getString(3);
								final int lengthCh = cursor.getInt(5);
								final int notes = cursor.getInt(6);

								final boolean end;
								if(i+number[length]<l){
									end = false;
									cursor.moveToPosition(i+number[length]);
								}
								else{
									end = true;
								}
								final String titlePa = cursor.getString(2);
								final String textPa = cursor.getString(3);
								final int lengthPa = cursor.getInt(5);
								Log.d("TAG", "refresh: "+i+"=>"+length+"/"+titleCh+"/"+idCh+"/"+timeCh+"////"+noCh+" of "+notes);


								final String metadataCh = "by "+idCh+", "+timeCh;

								View child = MainActivity.inflater.inflate(R.layout.activity_version_object, null);
								final TextView lengthChild = child.findViewById(R.id.lengthView);
								final TextView userChild = child.findViewById(R.id.userView);
								final TextView timeChild = child.findViewById(R.id.timeView);
								final ImageView editOnChild = child.findViewById(R.id.editOnView);
								final ImageView deleteOnChild = child.findViewById(R.id.deleteOnView);
								final ConstraintLayout ChildBackground = child.findViewById(R.id.VersionBackground);

								lengthChild.setText("  ->");
								userChild.setText("by "+idCh);
								timeChild.setText("at UTC "+timeCh);
								ChildBackground.setBackgroundColor(Color.parseColor("#DDDDDD"));
								deleteOnChild.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										final ConstraintLayout layout = (ConstraintLayout) View.inflate(VersionActivity.this, R.layout.dialog_delete, null);
										new AlertDialog.Builder(VersionActivity.this)
												.setView(layout)
												.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														dialog.dismiss();
														Toast.makeText(VersionActivity.this, "Version deleted", Toast.LENGTH_SHORT).show();
														SQLiteDatabase database = helper.getWritableDatabase();
														database.execSQL("delete from version where version="+noCh+";");
														database.close();
														refresh();
													}
												}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										}).show();
									}
								});
								editOnChild.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										if(level>1) {
											try {
												SecretKey key = new SecretKeySpec(AES_key, "AES");
												final Cipher decode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
												decode.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(Data.iv_def));

												final ConstraintLayout layout = (ConstraintLayout) View.inflate(VersionActivity.this, R.layout.dialog_mergecheck, null);
												new AlertDialog.Builder(VersionActivity.this)
														.setView(layout)
														.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int which) {
																dialog.dismiss();

																try{
																	Intent intent = new Intent(VersionActivity.this, NoteEditActivity.class);
																	intent.putExtra("AES_key", AES_key);
																	intent.putExtra("note", note);
																	intent.putExtra("add", noCh);
																	intent.putExtra("level", level);
																	intent.putExtra("baseTitle", new String(decode.doFinal(Base64.decode(titleCh,Base64.NO_WRAP))));
																	intent.putExtra("baseText", new String(decode.doFinal(Base64.decode(textCh,Base64.NO_WRAP))));
																	intent.putExtra("merge", true);
																	startActivity(intent);
																	finish();

																} catch (BadPaddingException | IllegalBlockSizeException e) {
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


											} catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
												e.printStackTrace();
											}
										}
										else{
											Toast.makeText(VersionActivity.this, "Level is too low", Toast.LENGTH_SHORT).show();
										}
									}
								});
								ChildBackground.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										Intent intent = new Intent(VersionActivity.this, NoteReadActivity.class);
										intent.putExtra("AES_key",AES_key);
										intent.putExtra("note",note);
										intent.putExtra("level",level);
										intent.putExtra("editable",false);
										intent.putExtra("end",end);
										intent.putExtra("add",noCh);
										intent.putExtra("title", titleCh);
										intent.putExtra("text", textCh);
										intent.putExtra("length", lengthCh);
										if(!end){
											intent.putExtra("title_origin", titlePa);
											intent.putExtra("text_origin", textPa);
											intent.putExtra("length_origin", lengthPa);
										}
										intent.putExtra("metadata", metadataCh);
										startActivityForResult(intent, 100);
									}
								});
								ChildContainer.addView(child);

							}
							i+=number[length];
							i--;
						}
						else{
							final boolean end;
							if(i+1<l){
								end = false;
								cursor.moveToPosition(i+1);
							}
							else{
								end = true;
							}
							final String titlePa = cursor.getString(2);
							final String textPa = cursor.getString(3);
							final int lengthPa = cursor.getInt(5);

							VersionBackground.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(VersionActivity.this, NoteReadActivity.class);
									intent.putExtra("AES_key",AES_key);
									intent.putExtra("note",note);
									intent.putExtra("level",level);
									intent.putExtra("editable",false);
									intent.putExtra("end",end);
									intent.putExtra("add",version);
									intent.putExtra("title", title);
									intent.putExtra("text", text);
									intent.putExtra("length", length);
									intent.putExtra("metadata", metadata);
									if(!end){
										intent.putExtra("title_origin", titlePa);
										intent.putExtra("text_origin", textPa);
										intent.putExtra("length_origin", lengthPa);
									}
									startActivityForResult(intent, 100);
								}
							});

						}
						ChildContainer.setVisibility(View.GONE);
						VersionContainerBottom.addView(view);
					}

					if(limit>0){
						View gap = MainActivity.inflater.inflate(R.layout.activity_versionempty_object, null);
						final TextView lengthStartView = gap.findViewById(R.id.lengthStartView);
						final TextView lengthEndView = gap.findViewById(R.id.lengthEndView);
						lengthStartView.setText(Integer.toString(0));
						lengthEndView.setText(Integer.toString(limit-1));
						VersionContainerBottom.addView(gap);
					}

					cursor.close();
					db.close();

				} catch (JSONException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
					e.printStackTrace();
					finish();
				}



			}
		};
		Map<String, String> map = new HashMap<>();
		map.put("method", "note_ends");
		map.put("note", Integer.toString(note));
		final AllRequest request = new AllRequest(map, listener);
		RequestQueue queue = Volley.newRequestQueue(this);
		queue.add(request);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode>100){
			finish();
		}
	}
}