object Version {
    val tag = cmd("git", "describe", "--tags", "HEAD")
    val hash = cmd("git", "rev-parse", "HEAD") ?: System.getProperty("softconfig.sourceRevision", "unknown")
    val shortHash = hash.take(7)
    val isSnapshot = tag == null || shortHash in tag
}
