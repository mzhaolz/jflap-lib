package(
  licenses =  ["notice"],
)

java_library(
	  name = "jflaplib_core",
	  srcs = glob(["**/*.java"], exclude = ["**/LRParser.java"]),
	  visibility = [ "//visibility:public" ],
	  deps = [
	  		"@com_google_guava//jar",
	  		"@org_apache_xmlgraphics_batik_anim//jar",
	  		"@org_apache_xmlgraphics_batik_dom//jar",
	  		"@org_apache_xmlgraphics_batik_svg_dom//jar",
	  		"@org_apache_xmlgraphics_batik_svggen//jar",
	  		"@org_apache_xmlgraphics_batik_swing//jar",
	  		"@org_apache_xmlgraphics_batik_awt_util//jar",
	  		"@org_apache_xmlgraphics_batik_util//jar",
	  		"@org_slf4j_slf4j_api//jar",
	  ]
)

COMMON_PREFIX = "//third_party/java/jflap-lib/jflaplib-core/"

java_binary(
    name = "jflap",
    srcs = glob([ "**/JFLAP.java" ]),
    deps = [
        ":jflaplib_core",
    ],
    main_class = "edu.duke.cs.jflap.JFLAP",
    resources = [
        COMMON_PREFIX + "src/main/resources/ICON:icons",
        COMMON_PREFIX + "src/main/resources/MEDIA:media",
        COMMON_PREFIX + "src/main/resources/DOCS:docs",
    ],
)
