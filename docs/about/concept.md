# Concepts

## Transformer

## Json

To manipulate Json, Streamy proposes its own Json AST because Json is the base of everything in Streamy.

### Json AST

The base type in Streamy is MaybeJson, and has several subtypes representing different Json types :
- JsUndefined : An undefined Json value. It is useful to avoid Option[Json]  pattern in some case and for performance reason.
- JsDefined : A defined Json value.
    - JsObject : A Json object, represented as a Map.
    - JsArray : A Json array, represented as an Array.
    - JsString : A Json String.
    - JsBoolean: A Json Boolean.
    - JsNull: A Json Null.
    - JsBytes : A Json Bytes.
    - JsNumber : A Json number, represented has several subtypes.
        - JsInt : A Json Int.
        - JsLong : A Json Long.
        - JsFloat : A Json Float.
        - JsDouble: A Json Double.
        - JsBigDecimal: A Json Big Decimal.

### Reading and writing json

The `io.techcode.streamy.util.json._`  package has several methods for reading and writing json.

#### Reading from ByteString

Parse a ByteString input into a Json value and will throw a ParseException in case of failure.

```scala
val result: Json = Json.parseByteStringUnsafe(ByteString("""
{
  "foo": "bar"
}
"""))
```

Parse a ByteString input into a Json value and will return an Either[Throwable, Json] value.

```scala
val result: Either[Throwable, Json] = Json.parseByteString(ByteString("""
{
  "foo": "bar"
}
"""))
```

#### Reading from String

Parse a String input into a Json value and will throw a ParseException in case of failure.

```scala
val result: Json = Json.parseStringUnsafe("""
{
  "foo": "bar"
}
""")
```

Parse a ByteString input into a Json value and will return an Either[Throwable, Json] value.

```scala
val result: Either[Throwable, Json] = Json.parseString("""
{
  "foo": "bar"
}
""")
```

#### Reading from Bytes
Parse an Array[Byte] input into a Json value and will throw a ParseException in case of failure.

```scala
val result: Json = Json.parseBytesUnsafe("""
{
  "foo": "bar"
}
""".getBytes())
```

Parse a ByteString input into a Json value and will return an Either[Throwable, Json] value.

```scala
val result: Either[Throwable, Json] = Json.parseBytes("""
{
  "foo": "bar"
}
""".getBytes())
```

#### Writing to ByteString

Print a Json input into a ByteString value and will throw a PrintException in case of failure.

```scala
val result: ByteString = Json.printByteStringUnsafe(Json.obj("foo" -> "bar"))
```

Print a Json input into a ByteString and will return aEither[Throwable, ByteString] value.

```scala
val result: Either[Throwable, ByteString] = Json.printByteString(Json.obj(
  "foo" -> "bar"
))
```

#### Writing to String

Print a Json input into a String value and will throw a PrintException in case of failure.

```scala
val result: String = Json.printStringUnsafe(Json.obj("foo" -> "bar"))
```

Print a Json input into a String and will return aEither[Throwable, String] value.

```scala
val result: Either[Throwable, String] = Json.printString(Json.obj(
  "foo" -> "bar"
))
```

### Manipulating Json AST

#### Evaluatea

#### Patch

## Parser

## Printer
