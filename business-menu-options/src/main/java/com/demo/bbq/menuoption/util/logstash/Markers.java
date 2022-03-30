package com.demo.bbq.menuoption.util.logstash;

import org.slf4j.Marker;

public final class Markers {

  public static final Marker SENSITIVE_JSON = net.logstash.logback.marker.Markers.append("sensitive", "json");
  public static final Marker SENSITIVE_TEXT = net.logstash.logback.marker.Markers.append("sensitive", "text");
  public static final Marker SENSITIVE_URL = net.logstash.logback.marker.Markers.append("sensitive", "url");
  public static final Marker SENSITIVE_TO_STRING = net.logstash.logback.marker.Markers.append("sensitive", "toString");
  public static final Marker SENSITIVE_MIXED = net.logstash.logback.marker.Markers.append("sensitive", "mixed");

  private Markers() {
  }
}
