package com.octahedron00.notebamboo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
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

public class RegisterActivity extends AppCompatActivity {

	EditText inputID, inputPassword, inputPassword2, inputNickname, inputEmail;
	Button button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		inputID = findViewById(R.id.inputID);
		inputPassword = findViewById(R.id.inputPassword);
		inputPassword2 = findViewById(R.id.inputPassword2);
		inputNickname = findViewById(R.id.inputNickname);
		inputEmail = findViewById(R.id.inputEmail);
		button = findViewById(R.id.listBack);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String id = inputID.getText().toString();
				final String password = inputPassword.getText().toString();
				String password2 = inputPassword2.getText().toString();
				final String nickname = inputNickname.getText().toString();
				String email = inputEmail.getText().toString();

				if(id.length()<4||20<id.length()){
					Toast.makeText(getApplicationContext(),"ID length : 4 ~ 20", Toast.LENGTH_SHORT).show();
				}
				else if(password.length()<4||20<password.length()){
					Toast.makeText(getApplicationContext(),"Password length : 4 ~ 20", Toast.LENGTH_SHORT).show();
				}
				else if(nickname.length()<1||20<nickname.length()){
					Toast.makeText(getApplicationContext(),"Nickname length : 1 ~ 20", Toast.LENGTH_SHORT).show();
				}
				else if(!password.equals(password2)){
					Toast.makeText(getApplicationContext(),"Confirm your password", Toast.LENGTH_SHORT).show();
				}
				else{
					String pw = password+"salt";

					//Password_SHA-256
					MessageDigest digest = null;
					try {
						digest = MessageDigest.getInstance("SHA-256");
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					digest.update(pw.getBytes());
					String pwEnc = Base64.encodeToString(digest.digest(),Base64.NO_WRAP);

					String random = pw+"-notebamboo-octahedron00-"+id;

					//RSA_key-generate
					KeyPair pair = null;
					String RSA_public="", RSA_private="", RSA_private_Enc="";
					try {
						KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
						generator.initialize(2048, new SecureRandom(random.getBytes()));
						pair = generator.generateKeyPair();
						RSA_private = Base64.encodeToString(pair.getPrivate().getEncoded(),Base64.NO_WRAP);
						RSA_public = Base64.encodeToString(pair.getPublic().getEncoded(),Base64.NO_WRAP);
						Log.i("tag", "onClick: "+pair.getPublic().getFormat());

						PrivateKey privateKey = pair.getPrivate();
						PublicKey publicKey = pair.getPublic();
						Log.i("key", "onClick: "+privateKey.getFormat()+publicKey.getFormat());

						Cipher rsaEnc = Cipher.getInstance("RSA/ECB/PKCS1Padding");
						Cipher rsaDec = Cipher.getInstance("RSA/ECB/PKCS1Padding");

						rsaEnc.init(Cipher.ENCRYPT_MODE,publicKey);
						String enc = Base64.encodeToString(rsaEnc.doFinal(password.getBytes()),Base64.NO_WRAP);

						rsaDec.init(Cipher.DECRYPT_MODE,privateKey);
						String dec = new String(rsaDec.doFinal(Base64.decode(enc,Base64.NO_WRAP)));

						rsaDec.init(Cipher.DECRYPT_MODE, KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKey.getEncoded())));
						String dec2 = new String(rsaDec.doFinal(Base64.decode(enc,Base64.NO_WRAP)));
						Log.d("keytest", "onClick: "+password);
						Log.d("keytest", "onClick: "+enc);
						Log.d("keytest", "onClick: "+dec);
						Log.d("keytest", "onClick: "+dec2);
					} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException e) {
						e.printStackTrace();
					}

					//IV_generate_by_MD5
					try {
						digest = MessageDigest.getInstance("MD5");
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					digest.update(pw.getBytes());
					byte[] iv = digest.digest();

					//RSA_privatekey_Encode
					String keyString = password+password+password+password+password+"NoteBambooNoteBambooNoteBambooNoteBamboo";
					byte[] keyByte = keyString.getBytes();
					byte[] keyByteTrim = new byte[32];
					for(int i=0; i<32; i++){
						keyByteTrim[i] = keyByte[i];
					}

					SecretKey key = new SecretKeySpec(keyByteTrim, "AES");
					try {
						Cipher sha;
						sha = Cipher.getInstance("AES/CBC/PKCS5PADDING");
						sha.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(iv));
						RSA_private_Enc = Base64.encodeToString(sha.doFinal(pair.getPrivate().getEncoded()), Base64.NO_WRAP);
					} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
						e.printStackTrace();
					}
					Log.d("tag", Base64.encodeToString(key.getEncoded(),2));
					Log.d("tag", "private " + RSA_private);
					Log.d("tag", "private_enc " + RSA_private_Enc);
					Log.d("tag", "public " + RSA_public);

					try {
						Cipher shadecode = Cipher.getInstance("AES/CBC/PKCS5PADDING");
						shadecode.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(iv));
						String RSA_private_Dec = Base64.encodeToString(shadecode.doFinal(Base64.decode(RSA_private_Enc,Base64.NO_WRAP)),Base64.NO_WRAP);
						Log.d("tag","private_dec " + RSA_private_Dec);
					} catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidAlgorithmParameterException e) {
						e.printStackTrace();
					}



					Response.Listener<String> listener = new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							Log.d("tag", response);
							try {
								JSONObject object = new JSONObject(response);
								boolean success = object.getBoolean("success");
								if(success){
									Toast.makeText(getApplicationContext(),"Register success", Toast.LENGTH_SHORT).show();
									MainActivity.editor.putString("NoteBamboo_id",id);
									MainActivity.editor.putString("NoteBamboo_password",password);
									MainActivity.editor.commit();
									finish();
								}
								else{
									String reason = object.getString("reason");
									Toast.makeText(getApplicationContext(),"Failed : "+reason, Toast.LENGTH_SHORT).show();
								}

							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					};
					Map<String, String> map = new HashMap();
					map.put("method", "register");
					map.put("id", id);
					map.put("pwEnc", pwEnc);
					map.put("nickname", nickname);
					map.put("email", email);
					map.put("RSA_public",RSA_public);
					map.put("RSA_private_Enc",RSA_private_Enc);
					AllRequest registerRequest = new AllRequest(map, listener);
					RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
					queue.add(registerRequest);

				}
			}
		});
	}
}