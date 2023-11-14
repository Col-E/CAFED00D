Classfile /C:/Code/Java/CAFED00D/tree/src/test/resources/samples/normal/javac/Inc.class
  Last modified Jun 9, 2023; size 699 bytes
  SHA-256 checksum 2be44a0911750729c4260345ccfbda6d35533b72b6b0ab565ba2476cea1eaca8
  Compiled from "Inc.java"
class Inc
  minor version: 0
  major version: 61
  flags: (0x0020) ACC_SUPER
  this_class: #8                          // Inc
  super_class: #2                         // java/lang/Object
  interfaces: 0, fields: 1, methods: 2, attributes: 1
Constant pool:
   #1 = Methodref          #2.#3          // java/lang/Object."<init>":()V
   #2 = Class              #4             // java/lang/Object
   #3 = NameAndType        #5:#6          // "<init>":()V
   #4 = Utf8               java/lang/Object
   #5 = Utf8               <init>
   #6 = Utf8               ()V
   #7 = Fieldref           #8.#9          // Inc.i:I
   #8 = Class              #10            // Inc
   #9 = NameAndType        #11:#12        // i:I
  #10 = Utf8               Inc
  #11 = Utf8               i
  #12 = Utf8               I
  #13 = Utf8               Code
  #14 = Utf8               LineNumberTable
  #15 = Utf8               LocalVariableTable
  #16 = Utf8               this
  #17 = Utf8               LInc;
  #18 = Utf8               incr
  #19 = Utf8               StackMapTable
  #20 = Utf8               SourceFile
  #21 = Utf8               Inc.java
{
  int i;
    descriptor: I
    flags: (0x0000)

  Inc();
    descriptor: ()V
    flags: (0x0000)
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 1: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   LInc;

  void incr();
    descriptor: ()V
    flags: (0x0000)
    Code:
      stack=3, locals=1, args_size=1
         0: aload_0
         1: dup
         2: getfield      #7                  // Field i:I
         5: iconst_1
         6: iadd
         7: putfield      #7                  // Field i:I
        10: aload_0
        11: dup
        12: getfield      #7                  // Field i:I
        15: iconst_1
        16: iadd
        17: putfield      #7                  // Field i:I
        20: aload_0
        21: dup
        22: getfield      #7                  // Field i:I
        25: iconst_1
        26: iadd
        27: putfield      #7                  // Field i:I
        30: aload_0
        31: dup
        32: getfield      #7                  // Field i:I
        35: iconst_1
        36: iadd
        37: putfield      #7                  // Field i:I
        40: aload_0
        41: dup
        42: getfield      #7                  // Field i:I
        45: iconst_1
        46: iadd
        47: putfield      #7                  // Field i:I
        50: aload_0
        51: dup
        52: getfield      #7                  // Field i:I
        55: iconst_1
        56: iadd
        57: putfield      #7                  // Field i:I
        60: aload_0
        61: dup
        62: getfield      #7                  // Field i:I
        65: iconst_1
        66: iadd
        67: putfield      #7                  // Field i:I
        70: aload_0
        71: getfield      #7                  // Field i:I
        74: iconst_3
        75: if_icmple     138
        78: aload_0
        79: dup
        80: getfield      #7                  // Field i:I
        83: iconst_1
        84: iadd
        85: putfield      #7                  // Field i:I
        88: aload_0
        89: dup
        90: getfield      #7                  // Field i:I
        93: iconst_1
        94: iadd
        95: putfield      #7                  // Field i:I
        98: aload_0
        99: dup
       100: getfield      #7                  // Field i:I
       103: iconst_1
       104: iadd
       105: putfield      #7                  // Field i:I
       108: aload_0
       109: dup
       110: getfield      #7                  // Field i:I
       113: iconst_1
       114: iadd
       115: putfield      #7                  // Field i:I
       118: aload_0
       119: dup
       120: getfield      #7                  // Field i:I
       123: iconst_1
       124: iadd
       125: putfield      #7                  // Field i:I
       128: aload_0
       129: dup
       130: getfield      #7                  // Field i:I
       133: iconst_1
       134: iadd
       135: putfield      #7                  // Field i:I
       138: aload_0
       139: dup
       140: getfield      #7                  // Field i:I
       143: iconst_1
       144: iadd
       145: putfield      #7                  // Field i:I
       148: aload_0
       149: dup
       150: getfield      #7                  // Field i:I
       153: iconst_1
       154: iadd
       155: putfield      #7                  // Field i:I
       158: aload_0
       159: dup
       160: getfield      #7                  // Field i:I
       163: iconst_1
       164: iadd
       165: putfield      #7                  // Field i:I
       168: aload_0
       169: dup
       170: getfield      #7                  // Field i:I
       173: iconst_1
       174: iadd
       175: putfield      #7                  // Field i:I
       178: aload_0
       179: dup
       180: getfield      #7                  // Field i:I
       183: iconst_1
       184: iadd
       185: putfield      #7                  // Field i:I
       188: aload_0
       189: dup
       190: getfield      #7                  // Field i:I
       193: iconst_1
       194: iadd
       195: putfield      #7                  // Field i:I
       198: aload_0
       199: dup
       200: getfield      #7                  // Field i:I
       203: iconst_1
       204: iadd
       205: putfield      #7                  // Field i:I
       208: aload_0
       209: dup
       210: getfield      #7                  // Field i:I
       213: iconst_1
       214: iadd
       215: putfield      #7                  // Field i:I
       218: aload_0
       219: dup
       220: getfield      #7                  // Field i:I
       223: iconst_1
       224: iadd
       225: putfield      #7                  // Field i:I
       228: aload_0
       229: dup
       230: getfield      #7                  // Field i:I
       233: iconst_1
       234: iadd
       235: putfield      #7                  // Field i:I
       238: aload_0
       239: dup
       240: getfield      #7                  // Field i:I
       243: iconst_1
       244: iadd
       245: putfield      #7                  // Field i:I
       248: return
      LineNumberTable:
        line 4: 0
        line 5: 10
        line 6: 20
        line 7: 30
        line 8: 40
        line 9: 50
        line 10: 60
        line 11: 70
        line 12: 78
        line 13: 88
        line 14: 98
        line 15: 108
        line 16: 118
        line 17: 128
        line 19: 138
        line 20: 148
        line 21: 158
        line 22: 168
        line 23: 178
        line 24: 188
        line 25: 198
        line 26: 208
        line 27: 218
        line 28: 228
        line 29: 238
        line 30: 248
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0     249     0  this   LInc;
      StackMapTable: number_of_entries = 1
        frame_type = 251 /* same_frame_extended */
          offset_delta = 138
}
SourceFile: "Inc.java"
