package me.bartosz1.web7;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
/*
  Representation of most of MIME types listed at https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types along with their file name extensions.
 */
public enum MimeType {

    AAC("audio/aac"), ABW("application/x-abiword"), ARC("application/x-freearc"), AVIF("image/avif"), AVI("video/x-msvideo"), AZW("application/vnd.amazon.ebook"), BIN("application/octet-stream"),
    BMP("image/bmp"), BZ("application/x-bzip"), BZ2("application/x-bzip2"), CDA("application/x-cdf"), CSH("application/x-csh"), CSS("text/css"), CSV("text/csv"), DOC("application/msword"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), EOT("application/vnd.ms-fontobject"), EPUB("application/epub+zip"), GZ("application/gzip"), GIF("image/gif"),
    HTM("text/html"), HTML("text/html"), ICO("image/vnd.microsoft.icon"), ICS("text/calendar"), JAR("application/java-archive"), JPEG("image/jpeg"), JPG("image/jpeg"), JS("text/javascript"),
    JSON("application/json"), JSONLD("application/ld+json"), MID("audio/midi"), MIDI("audio/midi"), MJS("text/javascript"), MP3("audio/mpeg"), MP4("video/mp4"), MPEG("video/mpeg"), MPKG("application/vnd.apple.installer+xml"),
    ODP("application/vnd.oasis.opendocument.presentation"), ODS("application/vnd.oasis.opendocument.spreadsheet"), ODT("application/vnd.oasis.opendocument.text"), OGA("audio/ogg"), OGV("audio/ogg"), OGX("application/ogg"), OPUS("audio/opus"),
    OTF("font/otf"), PNG("image/png"), PDF("application/pdf"), PHP("application/x-httpd-php"), PPT("application/vnd.ms-powerpoint"), PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation"), RAR("application/vnd.rar"),
    RTF("application/rtf"), SH("application/x-sh"), SVG("image/svg+xml"), SWF("application/x-shockwave-flash"), TAR("application/x-tar"), TIF("image/tiff"), TIFF("image/tiff"), TS("video/mp2t"), TTF("font/ttf"),
    TXT("text/plain"), VSD("application/vnd.visio"), WAV("audio/wav"), WEBA("audio/webm"), WEBM("video/webm"), WEBP("image/webp"), WOFF("font/woff"), WOFF2("font/woff2"), XHTML("application/xhtml+xml"),
    XLS("application/vnd.ms-excel"), XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), XML("application/xml"), XUL("application/vnd.mozilla.xul+xml"), ZIP("application/zip"), SEVENZIP("application/x-7z-compressed"),
    OTHER("application/octet-stream");
    private static final Map<String, MimeType> LOOKUP = new HashMap<>();

    static {
        for (MimeType type : MimeType.values()) {
            LOOKUP.put(type.getMimeType(), type);
        }
    }

    private final String mimeType;

    MimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @param file - The file to return MIME type for
     * @return corresponding MimeType enum value
     */
    public static MimeType getFromFileName(File file) {
        return getFromFileName(file.getName());
    }

    /**
     * @param name - file name to return MIME type for
     * @return corresponding MimeType enum value
     */
    public static MimeType getFromFileName(String name) {
        String[] split = name.split("\\.");
        String last = split[split.length - 1];
        try {
            if (last.equalsIgnoreCase("7Z")) return SEVENZIP;
            return valueOf(last.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }

    /**
     * @param s The MIME type as a String, for example "application/json"
     * @return corresponding MIME type enum value
     */
    public static MimeType getFromMimeTypeString(String s) {
        return LOOKUP.get(s);
    }

    /**
     * @return MIME type as a string, for example "application/json"
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return corresponding file extension or a blank string if called on OTHER value
     */
    public String getFileNameExtension() {
        return this == OTHER ? "" : name().toLowerCase();
    }
}
