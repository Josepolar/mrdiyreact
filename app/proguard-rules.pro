# Retrofit's legacy Gson API maps these model fields reflectively.
-keepclassmembers class com.mrdiy.careers.model.** { <fields>; }
# Supabase/kotlinx.serialization and AndroidX provide their own consumer rules.
# PDFBox's optional JPEG2000 image decoder is not used by text extraction.
# https://github.com/TomRoush/PdfBox-Android#optional-dependencies
-dontwarn com.gemalto.jp2.JP2Decoder
