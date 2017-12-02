# MIFARE Classic Refactor
This is the source code for the [MIFARE Classic Refactor] Android application.

Application features:
  * Change access keys and access conditions for MIFARE Classic® cards in a single read-write operation. 
  * Apply common change across all sectors, and/or specify changes per-sector.
  * Supports multiple key schemes - swipe to change between them. 

Note: You must have the current keys to the tag to be able to refactor it.

Bugs, feature suggestions and help requests can be filed with the [issue-tracker].

## License
[Apache 2.0]

Please note that MIFARE and MIFARE Classic are trademarks of NXP B.V.

# Obtain
The project is based on [Gradle] and is intended for use with [Android Studio].

# Usage
Run on a NFC-enabled device least version 4.0.3 (level 15). 

Note that the Mifare Classic tag is not supported in all phones due to differences in NFC chipset (hardware).

# History
While the app was originally created for fun in 2014, this reflects its life a an open source project.

 - [1.0.0]: Initial version

[MIFARE Classic Refactor]:       https://play.google.com/store/apps/details?id=com.skjolberg.nfc.mifareclassic
[1.0.0]:                releases
[Apache 2.0]:           http://www.apache.org/licenses/LICENSE-2.0.html
[issue-tracker]:        https://github.com/skjolber/xml-log-filter/issues
[Gradle]:               http://maven.apache.org/
[Android Studio]:       https://developer.android.com/studio/index.html
