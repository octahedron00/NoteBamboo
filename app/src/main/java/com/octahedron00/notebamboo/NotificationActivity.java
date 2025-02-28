package com.octahedron00.notebamboo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class NotificationActivity extends AppCompatActivity {

	LinearLayout notificationList;
	TextView emptyView;
	ImageView refreshNotification;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);

		notificationList = findViewById(R.id.notificationList);
		emptyView = findViewById(R.id.emptyView);
		refreshNotification = findViewById(R.id.refreshNotification);

		refreshNotification.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refresh();
			}
		});

		refresh();
	}

	public void refresh(){
		notificationList.removeAllViews();
		emptyView.setVisibility(View.VISIBLE);
		final Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(final String response) {
				Log.i("TAG", "onResponse: "+response);
				try {
					final JSONObject jsonObject = new JSONObject(response);
					JSONArray array = jsonObject.getJSONArray("array");
					notificationList.removeAllViews();
					if(array.length()>0){
						emptyView.setVisibility(View.GONE);
					}
					for(int i=0; i<array.length(); i++){
						JSONObject object = array.getJSONObject(i);

						final int no = object.getInt("no");
						final int list = object.getInt("list");
						final int level = object.getInt("level");
						final String AES_key = object.getString("AES_key");
						final int owner = object.getInt("owner");
						String from = object.getString("user_from");
						final String name = object.getString("name");



						PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Data.RSA_private);
						KeyFactory factory = KeyFactory.getInstance("RSA");
						PrivateKey key = factory.generatePrivate(keySpec);
						Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
						rsa.init(Cipher.DECRYPT_MODE,key);
						final String AES_real = Base64.encodeToString(rsa.doFinal(Base64.decode(AES_key,Base64.NO_WRAP)),Base64.NO_WRAP);

						Log.i("TAG", "onResponse: "+AES_real);
						SecretKey AESkey = new SecretKeySpec(Base64.decode(AES_real,Base64.NO_WRAP), "AES");
						Cipher decode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
						decode.init(Cipher.DECRYPT_MODE,AESkey,new IvParameterSpec(Data.iv_def));
						String title = new String(decode.doFinal(Base64.decode(name,Base64.NO_WRAP)));

						Log.d("tag","private_dec " + title);

						View view = MainActivity.inflater.inflate(R.layout.activity_notification_object, null);
						TextView titleText = view.findViewById(R.id.titleText);
						TextView describeText = view.findViewById(R.id.describeText);
						TextView levelText = view.findViewById(R.id.levelText);
						Button acceptButton = view.findViewById(R.id.acceptButton);
						Button declineButton = view.findViewById(R.id.declineButton);

						titleText.setText(title);
						describeText.setText("Invited by "+from);
						levelText.setText("Level "+level);


						SecretKey myKey = new SecretKeySpec(Data.user_key, "AES");
						Cipher encode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
						encode.init(Cipher.ENCRYPT_MODE,myKey,new IvParameterSpec(Data.iv));
						final String AES_mine = Base64.encodeToString(encode.doFinal(Base64.decode(AES_real,Base64.NO_WRAP)),Base64.NO_WRAP);
						final String name_Enc = Base64.encodeToString(encode.doFinal(title.getBytes()),Base64.NO_WRAP);

						acceptButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Response.Listener<String> deletion = new Response.Listener<String>() {
									@Override
									public void onResponse(String response) {
										Log.d("deletion", "onResponse: "+response);
									}
								};
								Map<String, String> map = new HashMap<>();
								map.put("method", "share_delete");
								map.put("no", Integer.toString(no));
								AllRequest notifRequest = new AllRequest(map, deletion);
								RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
								queue.add(notifRequest);

								Response.Listener<String> addition = new Response.Listener<String>() {
									@Override
									public void onResponse(String response) {
										try {
											JSONObject jsonResponse = new JSONObject(response);
											if(jsonResponse.getBoolean("success")){
												Toast.makeText(getApplicationContext(),"List added",Toast.LENGTH_SHORT).show();
											}
											else{
												Toast.makeText(getApplicationContext(),"List is already added",Toast.LENGTH_SHORT).show();
											}
											refresh();
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}
								};
								Log.d("TAG", "onClick: "+level+owner+name_Enc);
								map = new HashMap<>();
								map.put("method", "share_add");
								map.put("list", Integer.toString(list));
								map.put("user", Integer.toString(Data.user_no));
								map.put("level", Integer.toString(level));
								map.put("owner", Integer.toString(owner));
								map.put("name_Enc", name_Enc);
								map.put("AES_key_Enc", AES_mine);
								AllRequest shareRequest = new AllRequest(map, addition);
								queue.add(shareRequest);
							}
						});
						declineButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								final ConstraintLayout layout = (ConstraintLayout) View.inflate(NotificationActivity.this, R.layout.dialog_delete, null);
								new AlertDialog.Builder(NotificationActivity.this)
										.setView(layout)
										.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
												Response.Listener<String> listener = new Response.Listener<String>() {
													@Override
													public void onResponse(String response) {
														Toast.makeText(getApplicationContext(),"Invite message deleted",Toast.LENGTH_SHORT).show();
														refresh();
													}
												};
												Map<String, String> map = new HashMap<>();
												map.put("method", "share_delete");
												map.put("no", Integer.toString(no));
												AllRequest notifRequest = new AllRequest(map, listener);
												RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
												queue.add(notifRequest);
											}
										}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								}).show();
							}
						});
						notificationList.addView(view);
					}
				} catch (JSONException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException | InvalidAlgorithmParameterException e) {
					e.printStackTrace();
				}

			}
		};
		Map<String, String> map = new HashMap<>();
		map.put("method", "notifs");
		map.put("user", Integer.toString(Data.user_no));
		AllRequest request = new AllRequest(map, listener);
		RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
		queue.add(request);
	}
}