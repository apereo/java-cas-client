package org.jasig.cas.client.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

public class Strings
{
  public static List<String> csvToList(String csv_)
  {
    List<String> results = new ArrayList<String>();
    if (csv_ == null) return results;
    StringTokenizer st = new StringTokenizer(csv_, ",");
    while(st.hasMoreElements()){
    	results.add(st.nextElement().toString());
    }
    return results;
  }
  public static HashSet<String> csvToHashSet(String csv_) {
    HashSet<String> results = new HashSet<String>();
    if (csv_ == null) return results;
    StringTokenizer st = new StringTokenizer(csv_, ",");
    while(st.hasMoreElements()){
    	results.add(st.nextElement().toString());
    }return results;
  }
}
