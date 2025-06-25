module cafedude.core {
	requires static jakarta.annotation;
	requires transitive org.slf4j;

	opens software.coley.cafedude;
	opens software.coley.cafedude.classfile;
	opens software.coley.cafedude.classfile.annotation;
	opens software.coley.cafedude.classfile.attribute;
	opens software.coley.cafedude.classfile.behavior;
	opens software.coley.cafedude.classfile.constant;
	opens software.coley.cafedude.classfile.instruction;
	opens software.coley.cafedude.io;
	opens software.coley.cafedude.transform;
	opens software.coley.cafedude.util;

	exports software.coley.cafedude;
	exports software.coley.cafedude.classfile;
	exports software.coley.cafedude.classfile.annotation;
	exports software.coley.cafedude.classfile.attribute;
	exports software.coley.cafedude.classfile.behavior;
	exports software.coley.cafedude.classfile.constant;
	exports software.coley.cafedude.classfile.instruction;
	exports software.coley.cafedude.io;
	exports software.coley.cafedude.transform;
	exports software.coley.cafedude.util;
}