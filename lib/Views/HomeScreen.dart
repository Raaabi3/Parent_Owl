import 'dart:io';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class Homescreen extends StatefulWidget {
  const Homescreen({super.key});

  @override
  State<Homescreen> createState() => _HomescreenState();
}

class _HomescreenState extends State<Homescreen> {
  Uint8List? _screenshotImage;

  // Method channel for native functionality
  static const platform = MethodChannel('com.example.parent_owl/native');

  // Method to take a screenshot
  
Future<void> _takeScreenshot() async {
  try {
    final Uint8List? imageData = await platform.invokeMethod('takeScreenshot');
    if (imageData != null) {
      // Save to file for debugging
      final file = File('/sdcard/Download/screenshot.png');
      await file.writeAsBytes(imageData);
      print("Screenshot saved to: ${file.path}");

      setState(() {
        _screenshotImage = imageData;
      });
    }
  } on PlatformException catch (e) {
    print("Failed to take screenshot: ${e.message}");
  }
}

  // Method to lock the screen
  Future<void> _lockScreen() async {
    try {
      final bool result = await platform.invokeMethod('lockScreen');
      if (!result) {
        print("Device admin permission not granted");
      }
    } on PlatformException catch (e) {
      print("Failed to lock screen: ${e.message}");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Parent Owl")),
      body: SingleChildScrollView(
        child: Column(
          children: [
            ElevatedButton(
              onPressed: () async {
                await Future.delayed(Duration(milliseconds: 100)); // Add delay
                _takeScreenshot();
              },
              child: const Text("Take Screenshot"),
            ),
            ElevatedButton(
              onPressed: _lockScreen,
              child: const Text("Lock Screen"),
            ),
            if (_screenshotImage != null)
              Padding(
                padding: const EdgeInsets.all(30.0),
                child: Container(
                  decoration: BoxDecoration(border: Border.all()),
                  child: Image.memory(_screenshotImage!),
                ),
              ),
          ],
        ),
      ),
    );
  }
}
