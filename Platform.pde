static class Platform {
  // cache once → no repeated property look-ups
  private static final String OS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

  static boolean isWindows() { return OS.startsWith("windows"); }

  // On macOS the property may start with "Mac OS" or "Darwin"
  static boolean isMac()     { return OS.startsWith("mac") || OS.contains("darwin"); }

  // Most Unix flavours use one of these substrings
  static boolean isLinux()   { return OS.contains("linux") || OS.contains("nix")
                                    || OS.contains("nux")  || OS.contains("aix"); }

  static boolean isBSD()     { return OS.contains("freebsd")
                                    || OS.contains("openbsd")
                                    || OS.contains("netbsd"); }

  static boolean isSolaris() { return OS.contains("sunos"); }

  private Platform() {}            // prevent instantiation
}
