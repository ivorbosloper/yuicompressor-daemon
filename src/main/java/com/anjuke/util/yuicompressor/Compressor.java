package com.anjuke.util.yuicompressor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.io.StringReader;
import java.util.Map;
import java.util.HashMap;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Compressor {
	private static Map<String, String> cache = new HashMap<String, String>();

	public void setInputStream(InputStream value) {
		_in = value;
	}

	public void setOutputStream(OutputStream value) {
		_out = value;
	}

	public void compress() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(_in));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(_out));

			String filename = reader.readLine();
			if (cache == null) {
				this.process(filename, reader, writer);
				return;
			}

			StringBuilder sb = new StringBuilder();
			String s, aux, key;
			while ((aux = reader.readLine()) != null) {
				sb.append(aux).append('\n');
			}
			s = sb.toString();
			key = this.hash(s);
			if (cache.containsKey(key)) {
				writer.write(cache.get(key));
				return;
			}
			reader = new BufferedReader(new StringReader(s));
			StringWriter sw = new StringWriter();
			this.process(filename, reader, sw);
			s = sw.toString();
			
			cache.put(key, s);
			writer.write(s);
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void process(String filename, Reader reader, Writer writer)
			throws IOException {
		int index = filename.indexOf('.');
		String type = filename.substring(index + 1);
		if ("js".equalsIgnoreCase(type)) {
			processJavascript(reader, writer);
		} else if ("css".equalsIgnoreCase(type)) {
			processStyle(reader, writer);
		} else {
			throw new IOException("Illegal filename " + filename);
		}
		writer.close();
		reader.close();
	}

	protected String hash(String s) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(s.getBytes());
			byte[] bytes = md.digest();
			StringBuilder sb = new StringBuilder(bytes.length * 2);
			for (byte b : bytes) {
				sb.append(Integer.toHexString(b + 0x800).substring(1));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}

	}

	protected void processJavascript(Reader reader, Writer writer)
			throws IOException {
		JavaScriptCompressor compressor = new JavaScriptCompressor(reader,
				new ErrorReporter() {
					public void warning(String message, String sourceName,
							int line, String lineSource, int lineOffset) {
						System.out.println(String.format(
								"Warning: %s, %s, %d, %s, %d", message,
								sourceName, line, lineSource, lineOffset));
					}

					public void error(String message, String sourceName,
							int line, String lineSource, int lineOffset) {
						System.out.println(String.format(
								"Error: %s, %s, %d, %s, %d", message,
								sourceName, line, lineSource, lineOffset));
					}

					public EvaluatorException runtimeError(String message,
							String sourceName, int line, String lineSource,
							int lineOffset) {
						System.out.println(String.format(
								"Exception: %s, %s, %d, %s, %d", message,
								sourceName, line, lineSource, lineOffset));
						return new EvaluatorException(message);
					}
				});

		boolean verbose = false;
		int linebreakpos = -1;
		boolean munge = true;
		boolean preserveAllSemiColons = false;
		boolean disableOptimizations = false;
		compressor.compress(writer, linebreakpos, munge, verbose,
				preserveAllSemiColons, disableOptimizations);
	}

	protected void processStyle(Reader reader, Writer writer)
			throws IOException {
		CssCompressor compressor = new CssCompressor(reader);

		int linebreakpos = -1;
		compressor.compress(writer, linebreakpos);
	}

	private InputStream _in;
	private OutputStream _out;
}
