package(
  licenses =  ["notice"],
)

java_library(
	  name = "core",
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

java_binary(
    name = "jflap",
    srcs = glob([ "**/JFLAP.java" ]),
    deps = [
        ":core",
    ],
    main_class = "edu.duke.cs.jflap.JFLAP",
    resources = [
        "//core/src/main/resources/ICON:icons",
        "//core/src/main/resources/MEDIA:media",
        "//core/src/main/resources/DOCS:docs",
    ],
)
