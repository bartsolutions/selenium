// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.server.htmlrunner;

import org.openqa.jetty.http.HttpContext;
import org.openqa.jetty.http.HttpException;
import org.openqa.jetty.http.HttpHandler;
import org.openqa.jetty.http.HttpRequest;
import org.openqa.jetty.http.HttpResponse;
import org.openqa.jetty.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import java.nio.charset.Charset;


import java.nio.charset.StandardCharsets;


/**
 * Handles results of HTMLRunner (aka TestRunner, FITRunner) in automatic mode.
 *
 * @author Dan Fabulich
 * @author Darren Cotterill
 * @author Ajit George
 *
 */
@SuppressWarnings("serial")
public class SeleniumHTMLRunnerResultsHandler implements HttpHandler {
  static Logger log = Logger.getLogger(SeleniumHTMLRunnerResultsHandler.class.getName());

  HttpContext context;
  List<HTMLResultsListener> listeners;
  boolean started = false;

  public SeleniumHTMLRunnerResultsHandler() {
    listeners = new Vector<HTMLResultsListener>();
  }

  public void addListener(HTMLResultsListener listener) {
    listeners.add(listener);
  }

  public String toUTF8String(String input){
    byte[] bytes = input.getBytes(StandardCharsets.ISO_8859_1);
    String output =  new String(bytes, StandardCharsets.UTF_8);
    return output;
  }
  public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse res)
      throws HttpException, IOException {
    if (!"/postResults".equals(pathInContext)) return;
    request.setHandled(true);
    log.info("Received posted results");
    String result = request.getParameter("result");
    log.info("@67 ");
    log.info(result);
    log.info("@69");
    if (result == null) {
      res.getOutputStream().write("No result was specified!".getBytes());
    }
    res.setContentType("text/html; charset=UTF-8");

    log.info("@76");
    String seleniumVersion = request.getParameter("selenium.version");
    String seleniumRevision = request.getParameter("selenium.revision");
    String totalTime = request.getParameter("totalTime");
    String numTestTotal = request.getParameter("numTestTotal");
    String numTestPasses = request.getParameter("numTestPasses");
    String numTestFailures = request.getParameter("numTestFailures");
    String numCommandPasses = request.getParameter("numCommandPasses");
    String numCommandFailures = request.getParameter("numCommandFailures");
    String numCommandErrors = request.getParameter("numCommandErrors");
    String suite = request.getParameter("suite");
    String postedLog = request.getParameter("log");

    postedLog = this.toUTF8String(postedLog);
    suite = this.toUTF8String(suite);

    log.info("@89");
    int numTotalTests = Integer.parseInt(numTestTotal);

    List<String> testTables = createTestTables(request, numTotalTests);

    log.info("@94");

    //log.info(postedLog);

    log.info("@95");
    HTMLTestResults results =
        new HTMLTestResults(seleniumVersion, seleniumRevision,
            result, totalTime, numTestTotal,
            numTestPasses, numTestFailures, numCommandPasses, numCommandFailures, numCommandErrors,
            suite, testTables, postedLog);

    log.info("@101");
    for (Iterator<HTMLResultsListener> i = listeners.iterator(); i.hasNext();) {
      log.info("@103");
      HTMLResultsListener listener = i.next();
      log.info("@99");
      listener.processResults(results);
      log.info("@101");
      i.remove();
    }
    log.info("@102");
    processResults(results, res);
    log.info("@104");
  }

  /** Print the test results out to the HTML response */
  private void processResults(HTMLTestResults results, HttpResponse res) throws IOException {
//res.setContentType("text/html");
    res.setContentType("text/html; charset=UTF-8");
    //res.setCharacterEncoding("UTF-8");
    OutputStream out = res.getOutputStream();
    Writer writer = new OutputStreamWriter(out, StringUtil.__UTF_8);
//hWriter out = res.getWriter();
    log.info("@112");
    results.write(writer);
    writer.flush();
    log.info("@115");
  }

  private List<String> createTestTables(HttpRequest request, int numTotalTests) {
    List<String> testTables = new LinkedList<String>();
    for (int i = 1; i <= numTotalTests; i++) {
      String testTable = request.getParameter("testTable." + i);
      testTable = this.toUTF8String(testTable);
      // System.out.println("table " + i);
      // System.out.println(testTable);
      testTables.add(testTable);
    }
    return testTables;
  }

  public String getName() {
    return SeleniumHTMLRunnerResultsHandler.class.getName();
  }

  public HttpContext getHttpContext() {
    return context;
  }

  public void initialize(HttpContext c) {
    this.context = c;

  }

  public void start() throws Exception {
    started = true;
  }

  public void stop() throws InterruptedException {
    started = false;
  }

  public boolean isStarted() {
    return started;
  }
}



