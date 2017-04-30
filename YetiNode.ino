#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include <OneWire.h>
#include <DallasTemperature.h>


#define YL_PIN A0
#define YL_VCC D6
#define SR_04_TRIGGER D1
#define SR_04_ECHO D2
#define ONE_WIRE_BUS D4 

const char* ssid = "YetiNode";
const char* password = "A3QALM3J";
ESP8266WebServer server(80);

OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature DS18B20(&oneWire);

const int led = 13;

void handleRoot() {
  digitalWrite(led, 1);
  Serial.print("Humidity Level (0-1023): ");
  int soli_h = read_soild_humidity();
  Serial.println(soli_h); 
  
  Serial.print("Distance(cm): ");
  long d = read_distance();
  Serial.println(d);

  Serial.print("Temperature(éC): ");
  float t = read_temperature();
  Serial.println(t);
  
  String out = "{ \"soli_h\":" + String(soli_h) + ",\"d\":" + String(d) + ",\"t\":" + String(t) + "}";
  server.send(200, "text/plain", out);
  digitalWrite(led, 0);
}

void handleNotFound(){
  digitalWrite(led, 1);
  String message = "File Not Found\n\n";
  message += "URI: ";
  message += server.uri();
  message += "\nMethod: ";
  message += (server.method() == HTTP_GET)?"GET":"POST";
  message += "\nArguments: ";
  message += server.args();
  message += "\n";
  for (uint8_t i=0; i<server.args(); i++){
    message += " " + server.argName(i) + ": " + server.arg(i) + "\n";
  }
  server.send(404, "text/plain", message);
  digitalWrite(led, 0);
}

int read_soild_humidity() {
  digitalWrite(YL_VCC, HIGH);
  delay(500);
  int value = analogRead(YL_PIN);
  digitalWrite(YL_VCC, LOW);
  return 1023 - value;
}

float read_temperature() {
  DS18B20.requestTemperatures(); 
  float temp = DS18B20.getTempCByIndex(0);

  return temp;
}

long read_distance(){
  long duration, distance;
  digitalWrite(SR_04_TRIGGER, LOW);  // Added this line
  delayMicroseconds(2); // Added this line
  digitalWrite(SR_04_TRIGGER, HIGH);
  delayMicroseconds(10); // Added this line
  digitalWrite(SR_04_TRIGGER, LOW);
  
  duration = pulseIn(SR_04_ECHO, HIGH);
  distance = (duration/2) / 29.1;
 
  if (distance >= 200 || distance <= 0){
    distance = 0;
    Serial.println("Out of range");
  }
  delay(500);

  return distance;
}

void setup(void){
  // Init the humidity sensor board
  pinMode(YL_VCC, OUTPUT);
  digitalWrite(YL_VCC, LOW); 

  //init the distance sensor
  pinMode(SR_04_TRIGGER, OUTPUT);
  pinMode(SR_04_ECHO, INPUT);
  
  pinMode(led, OUTPUT);
  digitalWrite(led, 0);
  Serial.begin(115200);
  WiFi.begin(ssid, password);
  Serial.println("");

  // Wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.print("Connected to ");
  Serial.println(ssid);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  if (MDNS.begin("esp8266")) {
    Serial.println("MDNS responder started");
  }

  server.on("/", handleRoot);

  server.on("/inline", [](){
    server.send(200, "text/plain", "this works as well");
  });

  server.onNotFound(handleNotFound);

  server.begin();
  Serial.println("HTTP server started");
}

void loop(void){
  server.handleClient();
}
