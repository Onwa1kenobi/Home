#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <FirebaseArduino.h>
#include <FirebaseCloudMessaging.h>

// Define Firebase URL and SECRET.
#define FIREBASE_HOST "home-b39d9.firebaseio.com"
#define FIREBASE_AUTH "okgcoaUTc4yhpfXzOsS5TkxuyKuirzmMMZvGeyLB"
const String FCM_SERVER_KEY = "AAAAM3K-8lg:APA91bE08NMJpG0xCIhXz8GaOxdMhaSH6FqPJoPaXUjeoSoL5tkRBQlW9oa9YhRgVixR3S9HeYjrC3hsBjTwupwKWkB1AVZUdbzzFNq5X5B7WqKYue9RBK-qUbQHYael2xAF8b53hQ0l";

// Define WiFi connection credentials.
#define WIFI_SSID "Limited"
#define WIFI_PASSWORD "poiuytrewsx"

HTTPClient http;

// We'll create constants to name the special IDs designated to pins on our server.
const String HOME_ID_0 = "Julius/registeredIDs/home_id_0";
const String HOME_ID_1 = "Julius/registeredIDs/home_id_1";
const String HOME_ID_2 = "Julius/registeredIDs/home_id_2";
const String HOME_ID_3 = "Julius/registeredIDs/home_id_3";
const String HOME_SECURITY_TRIGGER = "Julius/isHomeSecured";
const String HOME_TEMPERATURE = "Julius/homeTemperature";
const String HOME_USER_FCM_TOKEN = "Julius/userInstanceTokenID";

// We'll create constants to name the pins we're using.
// This will make it easier to follow the code below.
const int pin0 = D0;
const int pin1 = D1;
const int pin2 = D2;
const int pin3 = D3;
const int analogSensorPin = A0;
const int securitySensorPin = D6;
const int pirSensorPin = D8;
const int buzzerPin = D7;
const int ldrMultiplexingPin = D4;
const int tempMultiplexingPin = D5;

// We'll also set up some global variables for the light level:

int lightLevel, high = 0, low = 1023;

// We'll set up an array with the notes we want to play with the alarm buzzer
// change these values to make different songs!
// Length must equal the total number of notes and spaces

const int songLength = 18;

// Notes is an array of text characters corresponding to the notes
// in the song. A space represents a rest (no tone)

char notes[] = "cdfda ag cdfdg gf "; // a space represents a rest

// Beats is an array of values for each note and rest.
// A "1" represents a quarter-note, 2 a half-note, etc.
// Don't forget that the rests (spaces) need a length as well.

int beats[] = {1, 1, 1, 1, 1, 1, 4, 4, 2, 1, 1, 1, 1, 1, 1, 4, 4, 2};

// The tempo is how fast to play the song.
// To make the song play faster, decrease this value.

int tempo = 150;

// The values for notes, beats and tempo are credited to SparkFun Electronics

int PIR_STATE = LOW;  // Start, assuming no motion is detected
int val = 0;  // Variable for reading the pin status

void setup() {
  Serial.begin(9600);

  // Setup microcontroller pins mode.
  setupPinsMode();

  // Connect to wifi.
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("connecting");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println();
  Serial.print("connected: ");
  Serial.println(WiFi.localIP());

  // Begin Firebase connection.
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}
int i = 0;
void loop() {
  digitalWrite(pin0, Firebase.getBool(HOME_ID_0));
  digitalWrite(pin1, Firebase.getBool(HOME_ID_1));
  digitalWrite(pin2, Firebase.getBool(HOME_ID_2));

  // set value
  Firebase.setFloat("number", i);
  i++;
  //
  multiplexTemperatureAndLightSensors();
  //
  if (Firebase.getBool(HOME_SECURITY_TRIGGER) == true) {
    // Enable PIR
    digitalWrite(pirSensorPin, HIGH);
    delay(5000);
    // Read PIR input value
    val = digitalRead(securitySensorPin);
    
    if (val == HIGH) {
      sendPushNotification("Alert!!!", "The motion sensor was triggered");
      delay(15000);
      if (Firebase.getBool(HOME_SECURITY_TRIGGER) == true) {
        triggerAlarmBuzzer(true);
      } else {
        triggerAlarmBuzzer(false);
        // Disable PIR
        digitalWrite(pirSensorPin, LOW);
      }
    } else {
      triggerAlarmBuzzer(false);
    }
  } else {
    // Disable PIR
    digitalWrite(pirSensorPin, LOW);
    triggerAlarmBuzzer(false);
  }
}

void setupPinsMode() {
  pinMode(pin0, OUTPUT);
  pinMode(pin1, OUTPUT);
  pinMode(pirSensorPin, OUTPUT);
  pinMode(securitySensorPin, INPUT);
  pinMode(analogSensorPin, INPUT);
  pinMode(buzzerPin, OUTPUT);
  pinMode(ldrMultiplexingPin, OUTPUT);
  pinMode(tempMultiplexingPin, OUTPUT);
}

void multiplexTemperatureAndLightSensors() {
  // Set all pins to LOW - no voltage
  digitalWrite(ldrMultiplexingPin, LOW);
  digitalWrite(tempMultiplexingPin, LOW);

  // Send voltage to LDR pin, completing the circuit for this sensor.
  digitalWrite(ldrMultiplexingPin, HIGH);
  // Short delay before reading, 100ms
  delay(100);
  // Perform the adc read function for this sensor
  performLightSensingFunction();
  // Return LDR pin to LOW - no voltage
  digitalWrite(ldrMultiplexingPin, LOW);
  // Short delay, 100ms
  delay(100);

  // Send voltage to Temperature pin, completing the circuit for this sensor.
  digitalWrite(tempMultiplexingPin, HIGH);
  // Short delay before reading, 100ms
  delay(100);
  // Perform the adc read function for this sensor
  performTemperatureReadingFunction();
  // Return Temperature pin to LOW - no voltage
  digitalWrite(tempMultiplexingPin, LOW);
  // Short delay, 100ms
  delay(100);
}

void performLightSensingFunction() {
  // We'll use the analogRead() function to measure the voltage
  // coming from the photoresistor resistor pair.
  // This number can range between 0 (0 Volts) and
  // 1023 (5 Volts).

  lightLevel = analogRead(analogSensorPin);

  // We now want to use this number to put ON external security lights
  // But we have a problem: the analogRead() function
  // returns values between 0 and 1023, and the analogWrite()
  // function wants values from 0 to 255.

  // We can solve this by using two handy functions called map()
  // and constrain(). Map will change one range of values into
  // another range. If we tell map() our "from" range is 0-1023,
  // and our "to" range is 0-255, map() will squeeze the larger
  // range into the smaller. (It can do this for any two ranges.)

  lightLevel = map(lightLevel, 0, 1023, 0, 255);

  // Because map() could still return numbers outside the "to"
  // range, (if they're outside the "from" range), we'll also use
  // a function called constrain() that will "clip" numbers into
  // a given range. If the number is above the range, it will reset
  // it to be the highest number in the range. If the number is
  // below the range, it will reset it to the lowest number.
  // If the number is within the range, it will stay the same.

  lightLevel = constrain(lightLevel, 0, 255);

  // Here's one last thing to think about. The circuit we made
  // won't have a range all the way from 0 to 5 Volts. It will
  // be a smaller range, such as 300 (dark) to 800 (light).
  // If we just pass this number directly to map(), the LED will
  // change brightness, but it will never be completely off or
  // completely on.

  autoTuneLightFunction();

  // The above functions will alter lightLevel to cover the
  // range from full-on to full-off. Now we can adjust the
  // brightness of the LED:

  analogWrite(pin3, 255 - lightLevel);
  Serial.print("Light sensor: ");
  Serial.println(255 - lightLevel);
  delay(500);

  // The above statement will brighten the LED in reverse proportion
  // with the light level.
  // Now we've created a night-light!
}

void autoTuneLightFunction()
{
  // As we mentioned above, the light-sensing circuit we built
  // won't have a range all the way from 0 to 1023. It will likely
  // be more like 300 (dark) to 800 (light).

  // In this function, the Arduino will keep track of the highest
  // and lowest values that we're reading from analogRead().

  // If you look at the top of the sketch, you'll see that we've
  // initialized "low" to be 1023. We'll save anything we read
  // that's lower than that:

  if (lightLevel < low)
  {
    low = lightLevel;
  }

  // We also initialized "high" to be 0. We'll save anything
  // we read that's higher than that:

  if (lightLevel > high)
  {
    high = lightLevel;
  }

  // Once we have the highest and lowest values, we can stick them
  // directly into the map() function.

  // One trick we'll do is to add a small offset to low and high,
  // to ensure that the LED is fully-off and fully-on at the limits
  // (otherwise it might flicker a little bit).

  lightLevel = map(lightLevel, low + 30, high - 30, 0, 255);
  lightLevel = constrain(lightLevel, 0, 255);

  // Now we'll return to the main loop(), and send lightLevel
  // to the LED.
}

void performTemperatureReadingFunction() {

  // We'll declare three floating-point variables

  float voltage, degreesC, degreesF;

  // First we'll measure the voltage at the analog pin. Normally
  // we'd use analogRead(), which returns a number from 0 to 1023.
  // Here we've written a function (further down) called
  // getVoltage() that returns the true voltage (0 to 5 Volts)
  // present on an analog input pin.

  voltage = getVoltage(analogSensorPin);

  // Now we'll convert the voltage to degrees Celsius.
  // This formula comes from the temperature sensor datasheet:

  degreesC = (voltage - 0.5) * 100.0;

  Firebase.setFloat(HOME_TEMPERATURE, degreesC);

  // Now we'll use the serial port to print these values
  // to the serial monitor!

  Serial.print("Voltage: ");
  Serial.print(voltage);
  Serial.print("  deg C: ");
  Serial.println(degreesC);
  delay(1000);
}

float getVoltage(int pin)
{

  // Here's the return statement for this function. We're doing
  // all the math we need to do within this statement:

  return (analogRead(pin) * 0.004882814);

  // This equation converts the 0 to 1023 value that analogRead()
  // returns, into a 0.0 to 5.0 value that is the true voltage
  // being read at that pin.
}

void triggerAlarmBuzzer(boolean state) {
  if (state == true) {
    int i, duration;

    for (i = 0; i < songLength; i++) { // step through the song arrays
      duration = beats[i] * tempo;  // length of note/rest in ms

      if (notes[i] == ' ') {         // is this a rest?
        delay(duration);            // then pause for a moment
      } else {                         // otherwise, play the note
        tone(buzzerPin, frequency(notes[i]), duration);
        delay(duration);            // wait for tone to finish
      }
      delay(tempo / 10);            // brief pause between notes
    }

    sendPushNotification("Alert!!!", "The motion sensor was triggered, and the alarm has been activated");
  } else {
    noTone(buzzerPin);
  }
}

int frequency(char note)
{
  // This function takes a note character (a-g), and returns the
  // corresponding frequency in Hz for the tone() function.

  int i;
  const int numNotes = 8;  // number of notes we're storing

  // The following arrays hold the note characters and their
  // corresponding frequencies. The last "C" note is uppercase
  // to separate it from the first lowercase "c". If you want to
  // add more notes, you'll need to use unique characters.

  // For the "char" (character) type, we put single characters
  // in single quotes.

  char names[] = { 'c', 'd', 'e', 'f', 'g', 'a', 'b', 'C' };
  int frequencies[] = {262, 294, 330, 349, 392, 440, 494, 523};

  // Now we'll search through the letters in the array, and if
  // we find it, we'll return the frequency for that note.

  for (i = 0; i < numNotes; i++)  // Step through the notes
  {
    if (names[i] == note)         // Is this the one?
    {
      return (frequencies[i]);    // Yes! Return the frequency
    }
  }
  return (0); // We looked through everything and didn't find it,
  // but we still need to return a value, so return 0.
}

//void sendPushNotification() {
//  FirebaseCloudMessaging fcm(FCM_SERVER_KEY);
//
//#define userToken "dD2CVlBNWnU:APA91bHkq5_bSG86hoWuQSIo7L3BqIS-SLbepaFjCSWSTdx2SZFfeAzuvDsOZYMny28UZmdCIeXh89BtEwmWUrhuN1T6N_cs9y3AGCkt-dOfuYsZ7wQi2tBFWDDVmdKf-yLdhxnFanzo"
//
//  FirebaseCloudMessage message =
//    FirebaseCloudMessage::SimpleNotification("Alert!!!", "The motion sensor was triggered");
//  FirebaseError error = fcm.SendMessageToUser(userToken, message);
//  if (error) {
//    Serial.print("Error:");
//    Serial.print(error.code());
//    Serial.print(" :: ");
//    Serial.println(error.message().c_str());
//    sendPushNotification();
//  } else {
//    Serial.println("Sent OK!");
//  }
//}

void sendPushNotification(String paytitle, String pay) {
  //more info @ https://firebase.google.com/docs/cloud-messaging/http-server-ref

  String userToken = Firebase.getString(HOME_USER_FCM_TOKEN);

  String data = "{";
  data = data + "\"to\": \"" + userToken + "\",";
  data = data + "\"notification\": {";
  data = data + "\"body\": \"" + pay + "\",";
  data = data + "\"title\" : \"" + paytitle + "\" ";
  data = data + "} }";

  http.begin("http://fcm.googleapis.com/fcm/send");
  http.addHeader("Authorization", "key=" + FCM_SERVER_KEY);
  http.addHeader("Content-Type", "application/json");
  http.addHeader("Host", "fcm.googleapis.com");
  http.addHeader("Content-Length", String(pay.length()));
  http.POST(data);
  http.writeToStream(&Serial);
  http.end();
  Serial.println();

}

