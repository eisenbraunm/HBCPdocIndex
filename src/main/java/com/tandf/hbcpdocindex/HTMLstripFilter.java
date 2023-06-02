package com.tandf.hbcpdocindex;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLstripFilter extends TokenFilter {

    private CharTermAttribute charTermAtt;
    private PositionIncrementAttribute poIncrAtt;

    public HTMLstripFilter(TokenStream input) {
        super(input);

        charTermAtt = addAttribute(CharTermAttribute.class);
        poIncrAtt = addAttribute(PositionIncrementAttribute.class);

        //poIncrAtt = addAttribute(PositionIncrementAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        // Loop over tokens in the token stream to find the next one
        // that is not empty
        String nextToken = null;
        while (nextToken == null) {

            // Reached the end of the token stream being processed
            if (!this.input.incrementToken()) {
                return false;
            }

            // Get text of the current token and remove any
            // leading/trailing whitespace.
            String currentTokenInStream =
                    this.input.getAttribute(CharTermAttribute.class)
                            .toString().trim();
            String oldStream = currentTokenInStream;

            currentTokenInStream = removeTagsPunct(currentTokenInStream);

            // Save the token if it is not an empty string
            if (currentTokenInStream.length() > 0) {
                nextToken = currentTokenInStream;
            }
        }

        // Save the current token
        charTermAtt.setEmpty().append(nextToken);
        poIncrAtt.setPositionIncrement(1);
        return true;

    }

    public static String removeTagsPunct(String string) {


        Pattern REMOVE_TAGS = Pattern.compile("<.+?>");
        if (string == null || string.length() == 0) {
            return "";
        }
        Matcher m = REMOVE_TAGS.matcher(string);

        String value = m.replaceAll("");

        value = value.replace(",", "");
        value = value.replace(";", "");
        value = value.replace(".", "");
        value = value.replace(":", "");
        value = value.replace("&gt;", ">");
        value = value.replace("&lt;", "<");
        value = value.replace("(", "");
        value = value.replace(")", "");
        value = value.replace("}", "");
        value = value.replace("{", "");
        value = value.replace("[", "");
        value = value.replace("]", "");
        value = value.replace("-", "");
        value=value.replace("”","");
        value=value.replace("“","");
        value=value.replace("?","");
        value=value.replace("*","");
        return value;
    }




}

