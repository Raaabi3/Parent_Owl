import 'package:flutter/services.dart';

class Homescreen extends StatelessWidget {
  const Homescreen({super.key});

  // Method channel for screen lock
  static const platform = MethodChannel('com.example.parent_owl/screen_lock');

  // Method to lock the screen
  Future<void> _lockScreen() async {
    try {
      await platform.invokeMethod('lockScreen');
    } on PlatformException catch (e) {
      print("Failed to lock screen: ${e.message}");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Parent Owl"),
      ),
      body: BlocBuilder<ToggleBloc, ToggleState>(
        builder: (context, state) {
          return Column(
            children: [
              ElevatedButton(
                onPressed: _lockScreen,
                child: const Text("Lock Screen"),
              ),
            ],
          );
        },
      ),
    );
  }
}