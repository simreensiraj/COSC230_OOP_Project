package model;

import java.util.*;

// anything we save to a file. each object = one line
// every class that implements this also has a static fromLine() to read the line back
public interface Storable {

    String toLine();

    // values are separated by commas. a comma, \ or new line inside a value
    // (e.g. in a description) gets a \ in front so it doesn't break the line
    static String join(String... values) {
        String line = "";
        for (int i = 0; i < values.length; i++) {
            String value = values[i] == null ? "" : values[i];
            value = value.replace("\\", "\\\\").replace(",", "\\,").replace("\n", "\\n");
            line += (i == 0 ? "" : ",") + value;
        }
        return line;
    }

    // undoes join(), and checks the line has the right number of values
    static String[] split(String line, int expectedValues) {
        ArrayList<String> values = new ArrayList<>();
        String current = "";
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\' && i + 1 < line.length()) {
                i++;
                current += (line.charAt(i) == 'n') ? "\n" : String.valueOf(line.charAt(i));
            } else if (c == ',') {
                values.add(current);
                current = "";
            } else {
                current += c;
            }
        }
        values.add(current);
        if (values.size() != expectedValues) {
            throw new IllegalArgumentException("expected " + expectedValues + " values but found "
                    + values.size());
        }
        return values.toArray(new String[0]);
    }
}
