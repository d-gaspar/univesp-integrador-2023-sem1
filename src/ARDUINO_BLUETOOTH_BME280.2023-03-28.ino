#include <Wire.h> // bme280
#include <Adafruit_Sensor.h> // bme280
#include <Adafruit_BME280.h> // bme280
#include <SoftwareSerial.h> // bluetooth

#define SEALEVELPRESSURE_HPA (1013.25)

SoftwareSerial bluetooth(11, 10); // RX, TX
Adafruit_BME280 bme;

char w;

void setup() {
  Serial.begin(9600); // PC
  bluetooth.begin(9600); // bluetooth
  Serial.println("Start system");
  
  if (!bme.begin(0x76)) {
    Serial.println("Could not find a valid BME280 sensor, check wiring!");
    while (1);
  }
}

void loop() {
  if (bluetooth.available()) {
    w = bluetooth.read(); // read bluetooth
    Serial.println(w); // print PC
    delay(10);
  }

  if (Serial.available()) {
    w = Serial.read(); // read PC
    bluetooth.println(w); // print bluetooth
    delay(10);
  }
  
  Serial.println("loop");

  bluetooth.print("T: ");
  bluetooth.print(bme.readTemperature());
  bluetooth.print("*C  ");

  bluetooth.print("H: ");
  bluetooth.print(bme.readHumidity());
  bluetooth.print("%  ");

  bluetooth.print("A: ");
  bluetooth.print(bme.readAltitude(SEALEVELPRESSURE_HPA));
  bluetooth.print("m  ");

  bluetooth.print("P: ");
  bluetooth.print(bme.readPressure() / 100.0F);
  bluetooth.print("hPa  ");
  
  bluetooth.println();
  
  delay(3000);

//  exit(0);
}
