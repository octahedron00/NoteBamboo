package com.octahedron00.notebamboo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context context) {
		super(context, "notebamboo", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table if not exists notes(" +
				"_id integer primary key autoincrement, " +
				"note integer not null, " +
				"length integer not null);";
		db.execSQL(sql);
		sql = "create table if not exists version(" +
				"_id integer primary key autoincrement, " +
				"version integer not null, " +
				"note integer not null, " +
				"title varchar(256) not null, " +
				"id varchar(64) not null, " +
				"text text, " +
				"user integer, " +
				"time varchar(64) not null, " +
				"length integer);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
