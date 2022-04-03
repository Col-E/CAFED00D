package me.coley.cafedude.transform;

/**
 * Set of JVM reserved opcodes.
 *
 * @author xDark
 */
public interface ReservedBytecodes {

	int breakpoint = 202;
	int fast_agetfield = 203;
	int fast_bgetfield = 204;
	int fast_cgetfield = 205;
	int fast_dgetfield = 206;
	int fast_fgetfield = 207;
	int fast_igetfield = 208;
	int fast_lgetfield = 209;
	int fast_sgetfield = 210;
	int fast_aputfield = 211;
	int fast_bputfield = 212;
	int fast_zputfield = 213;
	int fast_cputfield = 214;
	int fast_dputfield = 215;
	int fast_fputfield = 216;
	int fast_iputfield = 217;
	int fast_lputfield = 218;
	int fast_sputfield = 219;
	int fast_aload_0 = 220;
	int fast_iaccess_0 = 221;
	int fast_aaccess_0 = 222;
	int fast_faccess_0 = 223;
	int fast_iload = 224;
	int fast_iload2 = 225;
	int fast_icaload = 226;
	int fast_invokevfinal = 227;
	int fast_linearswitch = 228;
	int fast_binaryswitch = 229;
	int fast_aldc = 230;
	int fast_aldc_w = 231;
	int return_register_finalizer = 232;
}
