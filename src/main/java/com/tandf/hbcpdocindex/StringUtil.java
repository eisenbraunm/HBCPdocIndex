/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tandf.hbcpdocindex;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author Megan
 */
public class StringUtil {

    /**
     * Replaces characters out of a string
     * 
     * @param str the string to change
     * @param pattern what to replace
     * @param replace the replacement
     * @return the updated string
     */
    public static String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();

        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));
        return new String(result);
    }

    public static String toInitCap(String original) {
        if (original == null) {
            return null;
        } else if (original.length() < 1) {
            return original;
        }
        StringBuffer nameb = new StringBuffer(original.substring(0, 1).toUpperCase());
        nameb.append(original.substring(1));
        return nameb.toString();
    }

    /**
     * Pad to a line length to avoid line break within a word
     * 
     * @param str the string to change
     * @param fieldLen lenght of line to fit
     * @return the updated string
     */
    public static String noWordWrap(String str, int fieldLen) {

        String result = new String();
        String temp = "";
        StringTokenizer st = new StringTokenizer(str, " ", true);
        int tempLen = 0;

        while (st.hasMoreTokens()) {
            temp = st.nextToken();
            tempLen = temp.length();
            if (result.length() + tempLen < fieldLen) {
                result += temp;
            } else {
                for (int i = result.length(); i <= fieldLen; i++) {
                    result += " ";
                }
            }
        }
        return result;
    }

    /**
     * Convert the object to a string
     * 
     * @param o the Object to convert
     * @return the Object as string
     */
    public static String toString(Object o) {
        String s = null;
        if (o instanceof BigDecimal) {
            s = ((BigDecimal) o).toString();
        } else if (o instanceof Long) {
            s = ((Long) o).toString();

        } else if (o instanceof Boolean) {
            s = ((Boolean) o).toString();
        } else {
            if (o != null) {
                s = o.toString();
            }
        }
        return s;
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    //this adds 0 pad to the front of a number and returns a string
    public static String padNumberString(String Number, int reqLength) {


        while (Number.length() < reqLength) {
            Number = "0" + Number;
        }
        return Number;
    }
    
    public static String removeLeadingZeros( String str ){
     if (str == null){
           return null;
      }
      char[] chars = str.toCharArray();
      int index = 0;
      for (; index < str.length();index++){
           if (chars[index] != '0'){
                break;
           }
      }
     return (index == 0) ? str :str.substring(index);
}

    public static String accuracyNumberFormatted(Double Number, int accuracy) {

        BigDecimal bigd = BigDecimal.valueOf(Number);
        String formattedNumber = bigd.toPlainString();
        formattedNumber = formattedNumber.replaceAll("(\\.[0-9]*?)0*$", "$1");
        int dotIndex = formattedNumber.lastIndexOf(".");
        if (dotIndex == -1) {
            formattedNumber = formattedNumber + ".";
            dotIndex = formattedNumber.lastIndexOf(".");
        }
        int currentAcc = formattedNumber.length() - (dotIndex + 1);

        while ((formattedNumber.length() - (dotIndex + 1)) < accuracy) {
            formattedNumber = formattedNumber + "0";
        }

        if (accuracy == 0) {
            formattedNumber = formattedNumber.substring(0, dotIndex);
        }

        return formattedNumber;
    }

    public static Integer getAccuracy(String stringDouble) {


        Integer accuracy = 0;
        if (stringDouble.length() > 0) {
            int dotIndex = stringDouble.lastIndexOf(".");
            if (dotIndex >= 0) {
                stringDouble = stringDouble.substring(dotIndex + 1);
                accuracy = stringDouble.length();
            }
        }
        return accuracy;

    }

    public static String removeTags(String string) {
        Pattern REMOVE_TAGS = Pattern.compile("<.+?>");
        if (string == null || string.length() == 0) {
            return string;
        }
        Matcher m = REMOVE_TAGS.matcher(string);
        return m.replaceAll("");
    }

    public static boolean checkParentheses(String s) {
        int nesting = 0;
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
                case '(':
                    nesting++;
                    break;
                case ')':
                    nesting--;
                    if (nesting < 0) {
                        return false;
                    }
                    break;
            }
        }
        return nesting == 0;
    }

    public static boolean checkSqBrackets(String s) {
        int nesting = 0;
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
                case '[':
                    nesting++;
                    break;
                case ']':
                    nesting--;
                    if (nesting < 0) {
                        return false;
                    }
                    break;
            }
        }
        return nesting == 0;
    }
    

    
        //unescape entities except <,>, & ... replaces these with a holder then reinsert
    public static String unescapeEntites(String escapedString) {
        escapedString = escapedString.replace("&quot;", "foundQuote");
        escapedString = escapedString.replace("&#34;", "foundQuote");
        escapedString = escapedString.replace("&nbsp;", "nonBreakingSpace");
        escapedString = escapedString.replace("&#160;", "nonBreakingSpace");
        escapedString = escapedString.replace("&lt;", "foundLT");
        escapedString = escapedString.replace("&#60;", "foundLT");
        escapedString = escapedString.replace("&gt;", "foundGT");
        escapedString = escapedString.replace("&#62;", "foundGT");
        escapedString = escapedString.replace("&amp;", "foundAmperand");
        escapedString = escapedString.replace("&#38;", "foundAmperand");
        String unescapedString = StringEscapeUtils.unescapeHtml4(escapedString);
        unescapedString = unescapedString.replace("nonBreakingSpace", "&#160;");
        //the following are allowed in in HTML5
        unescapedString = unescapedString.replace("foundAmperand", "&amp;");
        unescapedString = unescapedString.replace("foundLT", "&lt;");
        unescapedString = unescapedString.replace("foundGT", "&gt;");
        unescapedString = unescapedString.replace("foundQuote", "&quot;");
        return unescapedString;
    }

    public static String escapeXml(String data) {
        if (data == null) {
            data = "";
        }
        String escapes[][] = {
            {
                "&", "&amp;"
            }, {
                ">", "&gt;"
            }, {
                "<", "&lt;"
            }, {
                "\"", "&quot;"
            }
        };
        for (int i = 0; i < escapes.length; i++) {
            data = StringUtils.replace(data, escapes[i][0], escapes[i][1]);
        }

        return data;
    }

    static public boolean isNumeric(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }



    public static void main(String[] args) {
     //   System.out.println("the formatted number  is: " + accuracyNumberFormatted(Double.parseDouble("10.00"), 2));

      String strHTMLInput = "This &amp; has superseded  &alpha; the older &quot;Fischer&quot; numbering which numbered only the eight &beta;-positions of the five membered pyrrole rings and labelled the four bridging &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <i>meso</i>-carbons &alpha;, &beta;, &gamma; and &delta;.<br /><br /><br /><br /";

   System.out.println(   unescapeEntites( strHTMLInput ));




    }
    
        public static String escapeUnicode2UTF8(String original) throws Exception {

        byte[] utf8Bytes = original.getBytes("UTF8");
        return new String(utf8Bytes, "UTF8");


    }

}
