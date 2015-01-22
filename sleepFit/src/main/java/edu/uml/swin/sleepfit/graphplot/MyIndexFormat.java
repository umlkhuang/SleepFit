package edu.uml.swin.sleepfit.graphplot;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public class MyIndexFormat extends Format {
	private static final long serialVersionUID = 31415L;
	public String[] labels = null;

	@Override
	public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
		int idx = ((Number) object).intValue();
		double val = ((Number) object).doubleValue();
		if (val >= idx + 0.5) idx += 1;
		if (labels == null || idx >= labels.length) 
			return new StringBuffer("");
		return new StringBuffer(labels[idx]);
	}

	@Override
	public Object parseObject(String string, ParsePosition position) {
		return null;
	}
}
