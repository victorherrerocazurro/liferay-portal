/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.servlet.filters.strip;

import com.liferay.portal.cache.key.HashCodeCacheKeyGenerator;
import com.liferay.portal.kernel.cache.key.CacheKeyGeneratorUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.util.MinifierUtil;

import java.io.StringWriter;

import java.nio.CharBuffer;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Shuyang Zhou
 * @author Miguel Pastor
 */
@PrepareForTest({CacheKeyGeneratorUtil.class, PropsUtil.class})
@RunWith(PowerMockRunner.class)
public class StripFilterTest extends PowerMockito {

	@Before
	public void setUp() {
		mockStatic(PropsUtil.class);

		when(PropsUtil.get(PropsKeys.TCK_URL)).thenReturn("tck.url");
	}

	@Test
	public void testHasMarker() {
		StripFilter stripFilter = new StripFilter();

		// Marker is longer than buffer's remaining

		CharBuffer charBuffer = CharBuffer.wrap("abcdef");

		charBuffer.position(2);
		charBuffer.limit(4);

		char[] marker = "cdef".toCharArray();

		Assert.assertFalse(stripFilter.hasMarker(charBuffer, marker));
		Assert.assertEquals(2, charBuffer.position());

		// No match

		charBuffer = CharBuffer.wrap("abcdef");
		marker = "abce".toCharArray();

		Assert.assertFalse(stripFilter.hasMarker(charBuffer, marker));
		Assert.assertEquals(0, charBuffer.position());

		// Exact match

		charBuffer = CharBuffer.wrap("abcdef");
		marker = "abcd".toCharArray();

		Assert.assertTrue(stripFilter.hasMarker(charBuffer, marker));
		Assert.assertEquals(0, charBuffer.position());

		// Match ignore case

		charBuffer = CharBuffer.wrap("aBcDef");
		marker = "abcd".toCharArray();

		Assert.assertTrue(stripFilter.hasMarker(charBuffer, marker));
		Assert.assertEquals(0, charBuffer.position());
	}

	@Test
	public void testProcessCSS() throws Exception {
		StripFilter stripFilter = new StripFilter();

		_mockCacheGenerationUtil();

		// Missing close tag

		CharBuffer charBuffer = CharBuffer.wrap("style type=\"text/css\">abc");

		StringWriter stringWriter = new StringWriter();

		stripFilter.processCSS(null, null, charBuffer, stringWriter);

		Assert.assertEquals(
			"style type=\"text/css\">", stringWriter.toString());
		Assert.assertEquals(22, charBuffer.position());

		// Empty tag

		charBuffer = CharBuffer.wrap("style type=\"text/css\"></style>");
		stringWriter = new StringWriter();

		stripFilter.processCSS(null, null, charBuffer, stringWriter);

		Assert.assertEquals(
			"style type=\"text/css\"></style>", stringWriter.toString());
		Assert.assertEquals(30, charBuffer.position());

		// Minifier spaces

		charBuffer = CharBuffer.wrap("style type=\"text/css\"> \r\t\n</style>");
		stringWriter = new StringWriter();

		stripFilter.processCSS(null, null, charBuffer, stringWriter);

		Assert.assertEquals(
			"style type=\"text/css\"></style>", stringWriter.toString());
		Assert.assertEquals(34, charBuffer.position());

		// Minifier code

		String code =
			".a{ position: relative; outline: none; overflow: " +
				"hidden; text-align: left /* Force default alignment */ }";
		String minifiedCode = MinifierUtil.minifyCss(code);

		charBuffer = CharBuffer.wrap(
			"style type=\"text/css\">" + code + "</style>");
		stringWriter = new StringWriter();

		stripFilter.processCSS(null, null, charBuffer, stringWriter);

		Assert.assertEquals(
			"style type=\"text/css\">" + minifiedCode + "</style>",
			stringWriter.toString());
		Assert.assertEquals(code.length() + 30, charBuffer.position());

		// Minifier code with trailing spaces

		charBuffer = CharBuffer.wrap(
			"style type=\"text/css\">" + code + "</style> \r\t\n");
		stringWriter = new StringWriter();

		stripFilter.processCSS(null, null, charBuffer, stringWriter);

		Assert.assertEquals(
			"style type=\"text/css\">" + minifiedCode + "</style> ",
			stringWriter.toString());
		Assert.assertEquals(code.length() + 34, charBuffer.position());

		verifyStatic(Mockito.times(3));
	}

	@Test
	public void testProcessJavaScript() throws Exception {
		StripFilter stripFilter = new StripFilter();

		_mockCacheGenerationUtil();

		// Missing close tag

		CharBuffer charBuffer = CharBuffer.wrap("script>abc");
		StringWriter stringWriter = new StringWriter();

		stripFilter.processJavaScript(
			charBuffer, stringWriter, "script".toCharArray());

		Assert.assertEquals("script>", stringWriter.toString());
		Assert.assertEquals(7, charBuffer.position());

		// Empty tag

		charBuffer = CharBuffer.wrap("script></script>");
		stringWriter = new StringWriter();

		stripFilter.processJavaScript(
			charBuffer, stringWriter, "script".toCharArray());

		Assert.assertEquals("script></script>", stringWriter.toString());
		Assert.assertEquals(16, charBuffer.position());

		// Minifier spaces

		charBuffer = CharBuffer.wrap("script> \t\r\n</script>");
		stringWriter = new StringWriter();

		stripFilter.processJavaScript(
			charBuffer, stringWriter, "script".toCharArray());

		Assert.assertEquals("script></script>", stringWriter.toString());
		Assert.assertEquals(20, charBuffer.position());

		// Minifier code

		String code = "function(){ var abcd; var efgh; }";
		String minifiedCode = MinifierUtil.minifyJavaScript(code);

		charBuffer = CharBuffer.wrap("script>" + code + "</script>");
		stringWriter = new StringWriter();

		stripFilter.processJavaScript(
			charBuffer, stringWriter, "script".toCharArray());

		Assert.assertEquals(
			"script>/*<![CDATA[*/" + minifiedCode + "/*]]>*/</script>",
			stringWriter.toString());
		Assert.assertEquals(code.length() + 16, charBuffer.position());

		// Minifier code with trailing spaces

		charBuffer = CharBuffer.wrap("script>" + code + "</script> \t\r\n");
		stringWriter = new StringWriter();

		stripFilter.processJavaScript(
			charBuffer, stringWriter, "script".toCharArray());

		Assert.assertEquals(
			"script>/*<![CDATA[*/" + minifiedCode + "/*]]>*/</script> ",
			stringWriter.toString());
		Assert.assertEquals(code.length() + 20, charBuffer.position());

		verifyStatic(Mockito.times(5));
	}

	@Test
	public void testProcessPre() throws Exception {
		StripFilter stripFilter = new StripFilter();

		// Missing close tag

		CharBuffer charBuffer = CharBuffer.wrap("pre>abcde");
		StringWriter stringWriter = new StringWriter();

		stripFilter.processPre(charBuffer, stringWriter);

		Assert.assertEquals("pre", stringWriter.toString());
		Assert.assertEquals(3, charBuffer.position());

		// Without trailing spaces

		charBuffer = CharBuffer.wrap("pre>a b </pre>");
		stringWriter = new StringWriter();

		stripFilter.processPre(charBuffer, stringWriter);

		Assert.assertEquals("pre>a b </pre>", stringWriter.toString());
		Assert.assertEquals(14, charBuffer.position());

		// With trailing spaces

		charBuffer = CharBuffer.wrap("pre>a b </pre> \r\n\tc");
		stringWriter = new StringWriter();

		stripFilter.processPre(charBuffer, stringWriter);

		Assert.assertEquals("pre>a b </pre> ", stringWriter.toString());
		Assert.assertEquals(18, charBuffer.position());
	}

	@Test
	public void testProcessTextArea() throws Exception {
		StripFilter stripFilter = new StripFilter();

		// Missing close tag

		CharBuffer charBuffer = CharBuffer.wrap("textarea >abcde");
		StringWriter stringWriter = new StringWriter();

		stripFilter.processTextArea(charBuffer, stringWriter);

		Assert.assertEquals("textarea ", stringWriter.toString());
		Assert.assertEquals(9, charBuffer.position());

		// Without trailing spaces

		charBuffer = CharBuffer.wrap("textarea >a b </textarea>");
		stringWriter = new StringWriter();

		stripFilter.processTextArea(charBuffer, stringWriter);

		Assert.assertEquals(
			"textarea >a b </textarea>", stringWriter.toString());
		Assert.assertEquals(25, charBuffer.position());

		// With trailing spaces

		charBuffer = CharBuffer.wrap("textarea >a b </textarea> \r\n\tc");
		stringWriter = new StringWriter();

		stripFilter.processTextArea(charBuffer, stringWriter);

		Assert.assertEquals(
			"textarea >a b </textarea> ", stringWriter.toString());
		Assert.assertEquals(29, charBuffer.position());
	}

	@Test
	public void testSkipWhiteSpace() throws Exception {
		StripFilter stripFilter = new StripFilter();

		// Empty buffer

		CharBuffer charBuffer = CharBuffer.allocate(0);
		StringWriter stringWriter = new StringWriter();

		stripFilter.skipWhiteSpace(charBuffer, stringWriter, true);

		Assert.assertEquals("", stringWriter.toString());
		Assert.assertEquals(0, charBuffer.position());

		// No leading space

		charBuffer = CharBuffer.wrap("abc \t\r\n");
		stringWriter = new StringWriter();

		stripFilter.skipWhiteSpace(charBuffer, stringWriter, true);

		Assert.assertEquals("", stringWriter.toString());
		Assert.assertEquals(0, charBuffer.position());

		// Single leading space

		charBuffer = CharBuffer.wrap(" ");
		stringWriter = new StringWriter();

		stripFilter.skipWhiteSpace(charBuffer, stringWriter, true);

		Assert.assertEquals(" ", stringWriter.toString());
		Assert.assertEquals(1, charBuffer.position());

		charBuffer = CharBuffer.wrap("\t");
		stringWriter = new StringWriter();

		stripFilter.skipWhiteSpace(charBuffer, stringWriter, true);

		Assert.assertEquals(" ", stringWriter.toString());
		Assert.assertEquals(1, charBuffer.position());

		charBuffer = CharBuffer.wrap("\r");
		stringWriter = new StringWriter();

		stripFilter.skipWhiteSpace(charBuffer, stringWriter, true);

		Assert.assertEquals(" ", stringWriter.toString());
		Assert.assertEquals(1, charBuffer.position());

		charBuffer = CharBuffer.wrap("\n");
		stringWriter = new StringWriter();

		stripFilter.skipWhiteSpace(charBuffer, stringWriter, true);

		Assert.assertEquals(" ", stringWriter.toString());
		Assert.assertEquals(1, charBuffer.position());

		// Multiple leading spaces

		charBuffer = CharBuffer.wrap(" \t\r\n");
		stringWriter = new StringWriter();

		stripFilter.skipWhiteSpace(charBuffer, stringWriter, true);

		Assert.assertEquals(" ", stringWriter.toString());
		Assert.assertEquals(4, charBuffer.position());
	}

	private void _mockCacheGenerationUtil() {
		mockStatic(CacheKeyGeneratorUtil.class);

		when(
			CacheKeyGeneratorUtil.getCacheKeyGenerator(
				StripFilter.class.getName())
		).thenReturn(
			new HashCodeCacheKeyGenerator()
		);
	}

}