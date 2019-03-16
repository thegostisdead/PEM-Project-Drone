# Drone API

>All the main API, the Picture WebInterface and the WebSocket data exchange code will be here.

## Table of Content

 - [Main API](#main-api)
	- [Web Settings API](#web-settings-api)
	- [Physical Qualities API](#physical-qualities-api)
 - [Picture WebInterface](#picture-webinterface)
 - [WebSocket Real-Time Data Exchanger](#websocket-real-time-data-exchanger)
 

## Main API

>The API will be handling and storing received data from the external source.

>If you send a GET request to the base endpoint ("/"), you will able to get a little bit of information about the API.

```json
{
	"author": "Enzo CACERES",
	"api": "Drone API",
	"version": {
		"id": "0.1",
		"type": "BETA"
	}
}
```

### Web Settings API

#### Description

>The Web Settings API is a simple API able to simply store the settings that are changable on the frontend and get them back as easely.

#### Usage

##### Getting the Settings

>To get all the available settings, you will have to do a GET request on the ``/settings`` endpoint.
>You will receive a JSON containing all key value available.

```json
[
	{
		"key": "hello",
		"value": "world"
	},
	{
		"key": "from",
		"value": "json"
	}
]
```

##### Updating the Settings

>To update a single or multiple values, you will have to do a POST request on the same endpoint.
>With a body (in JSON) that must contain the settings that you want to update:

```json
[
	{
		"key": "hello",
		"value": "world2"
	}
]
```

>By posting this, you will receive:

```json
{
	"updated_items": [
		{
			"key": "hello",
			"value": "world2"
		}
	]
}
```

>This response will help you to know what values have been updated.  
>Useful when you want to do an animation if this particular settings has been updated.  
>Be aware that only values that have been changed or created will be shown, if a previously created settings has been resent again, it will not be displayed in the "updated_items" array.  

### Physical Qualities API

#### Description

>The Physical Qualities API is very similar to the Web Settings API.  
>But this time, all data are stored in a SQLite file.  

>You will only be able to push data to physical qualities that have already been registered before. If you try with a non-existing one, it will simple not process it and skip to the next (if you have send multiple new value in the same request).

#### Usage

##### Getting a list of all registered physical qualities

>By simply doing a GET request on the ``/settings/list`` endpoint, you will be able to get a list of the currently registed and loaded physical qualities to push data on.

>Here is a response from the default config:

```json
{
	"loaded": [
		{
			"unit": "°C",
			"name": "temperature"
		}
	]
}
```

##### Getting the Stored Values

>Be aware that you will only be able to get the last 60 values that are stored the database. But this number is configurable in the config file.
>By doing a GET request to the ``/qualities`` endpoint, you will get back all the last 60 values from every registered physical qualities.

>Here is a result of what you can get:

```json
{
	"temperature": {
		"name": "temperature",
		"unit": "°C",
		"values": [
			{
				"date": 1552756197135,
				"content": "18.8"
			},
			{
				"date": 1552756154252,
				"content": "18.9"
			}
		]
	}
}
```

##### Pushing new values to store

>To push new values to the API storage, you will have to do a POST request on the ``/settings/push`` endpoint.

>With the following body, you will add the value "19" in the "temperature" physical quality at the Sat Mar 16 2019 at 17:15:48.

```json
{
	"temperature": [
		{
			"date": 1552756548654,
			"content": "19"
		}
	]
}
```

>But you can also combine multiple physical qualities and multiple values at once at the same time:

```json
{
	"temperature": [
		{
			"date": 1552756548654,
			"content": "19"
		}
	],
	"other": [
		{
			"date": 1552756548654,
			"content": "65"
		},
		{
			"date": 1552756548655,
			"content": "63"
		}
	]
}
```

## Picture WebInterface

>The Picture WebInterface is like a small dedicated Web Server that will able to only process to a special request.

#### How does it works?

>The system use Sockets to transmit binary data.  
>It will first read all bytes it found until a new line character is found. That will be how it will differentiate the header from the rest of the picture data.  
>When the header has been processed, the rest of the file will be read and the socket will be closed. Only a certain number of byte will be readed, this number is determined by the "length" header key. If you try to send more data than it can accept (over the "length" limit) it will cut off before the end.  

#### Header

>To send a Picture to the WebInterface, you will have to include a small and simple header a the beginning the data that you send to allow the logic to handle it more easely.  

|  Keys  	|        Arg. 1       	|   Arg. 2  	|                                                       Description                                                      	|
|:------:	|:-------------------:	|:---------:	|:----------------------------------------------------------------------------------------------------------------------:	|
|   id   	|     picture name    	|           	| Send a custom name to the server                                                                                       	|
| length 	| file length in byte 	|           	| Tell the server how much the image is. If you try to send a picture bigger than this value, it will cut off before end 	|
|   gps  	|       latitude      	| longitude 	| Precise position where the picture has been tooked                                                                     	|                                                                 	|

>All keys-value must be separated from each other with a colon (",").  
>All arguments must be separated from each other with a space (" ").  
>The header must finish with a new line character before you send your picture raw data ("\n").  

#### Exemple

>Lets try to send an image that the server must name "hello", with a file size equal to 105433 bytes and that have been taken with some random GPS position:

```
id hello,length 105433,gps 1.23456 6.54321\n
```

## WebSocket Real-Time Data Exchanger

>(not ready yet)
