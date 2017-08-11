package james.medianotification.utils;

public class MarkdownUtils {

    public static String toHtml(String markdownString) {
        markdownString = replace(markdownString, "**", "<b>", "</b>");
        markdownString = replace(markdownString, "*", "<i>", "</i>");
        markdownString = markdownString.replace("---", "\n");
        markdownString = markdownString.replace("-", "\u2022");
        markdownString = markdownString.replace("\n", "<br>");
        return markdownString;
    }

    private static String replace(String string, String occurrence, String... replacements) {
        for (int i = 0; string.contains(occurrence); i++) {
            int index = string.indexOf(occurrence);
            String replacement = replacements[i % replacements.length];
            string = string.substring(0, index) + replacement + string.substring(index + occurrence.length());
        }

        return string;
    }

}
