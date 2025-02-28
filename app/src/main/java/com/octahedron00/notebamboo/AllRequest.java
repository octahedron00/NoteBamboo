package com.octahedron00.notebamboo;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

public class AllRequest extends StringRequest {
	final static private String URL = "http://34.173.252.233//app.php";
	private final Map<String,String> parameters;

	public AllRequest(Map<String, String> param, Response.Listener<String> listener) {
		super(Method.POST, URL, listener, null);
		parameters = param;
	}

	@Override
	public Map<String,String> getParams(){
		return parameters;
	}
}
