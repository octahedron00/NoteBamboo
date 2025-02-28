package com.octahedron00.notebamboo;

import android.content.Context;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

public class TextEditor extends androidx.appcompat.widget.AppCompatEditText {
	public TextEditor(@NonNull Context context) {
		super(context);
	}

	public TextEditor(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public TextEditor(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		int start = selStart;
		int end = selEnd;
		Log.i("span", "onFocusChange: "+start+" / "+end+" / "+ Arrays.toString(this.getText().getSpans(start, end, StyleSpan.class)));

		super.onSelectionChanged(selStart, selEnd);
	}

	public void doBold(){

	}
}
