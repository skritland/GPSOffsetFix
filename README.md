GPSOffsetFix
============

Description
-----------
GPSOffsetFix is small module for Xposed framework to correct problems with maps offset in China.

Because of some military relic many (if not all) maps like GoogleMaps, BingMaps, Here, etc. are distorted a little. It means that position on the map and real position differs by some small offset. The offset is generally less than 1 km but is not constant over the China. Using formula from [1] offset can be reduced to several meters.

GPSOffsetFix adds some offset calculated using formula from [1] to location given by android. Applications are not aware that location is modified. This will fix your position on the map but position log will be false. If you can it is better to use correct maps.

Currently module is in early development stage but shortly it will be usable.

Current version
---------------
0.1.0 Beta

Usage
-----
Just install it, turn it on in Xposed installer and reboot your phone. Remember it is still in **BETA** stage.

**IMPORTANT**
-------------
Don't forget to turn off this module in Xposed installer when you are not in China. Detecting if you are in China is very simple (rectangular bounding box) so if you don't turn it off then your location will be false.

GPSOffsetFix changes position for every application which is using specified APIs. Position logging will be incorrect (will contain shifted data) - it will display correctly on for example GoogleMaps (because GoogleMaps are shifted) but it will NOT contain REAL position.

Technical data
--------------
Android applications can use different methods to obtain current location. Not every method is supported by the module. Below list of currently supported location APIs:
- LocationManager using LocationListener

Still **NOT** supported but known methods:
- LocationManager using PendingIntent
- FusedLocationApi
- LocationClient (deprecated by Google)
- GoogleMaps api (deprecated by Google)


Thanks
------
- authors of used coordinates transformation [1]
- authors of XPrivacy [2] - general idea of this module has been taken from them

Links
-----
- [1] https://on4wp7.codeplex.com/SourceControl/changeset/view/21455#EvilTransform.cs
- [2] https://github.com/M66B/XPrivacy

License
-------
Copyright {2015} {skritland}

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
