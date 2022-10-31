package com.winning.pbc.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class IdeaXmlWriter extends XMLWriter {
  public IdeaXmlWriter(File file) throws IOException {
    super(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"), OutputFormat.createPrettyPrint());
  }

  protected String escapeAttributeEntities(String text) {
    String answer = super.escapeAttributeEntities(text);
    answer = answer.replaceAll("\n", "&#10;");
    answer = answer.replaceAll("\n\r", "&#10;");
    return answer;
  }
}
