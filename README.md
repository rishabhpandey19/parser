# parser

A generic, configurable CLI that parses **HTML (file/URL)**, **XML**, and **JSON**
and extracts the parts you care about, using **named rules** defined in
`application.yml`.

Built with Spring Boot + picocli. JSON uses JSONPath, HTML uses CSS selectors (Jsoup),
and XML uses XPath.

## Build

```bash
mvn clean package
# produces target/parser-0.0.1-SNAPSHOT.jar (a runnable fat jar)
```

Run tests:

```bash
mvn test
```

## Usage

```bash
java -jar target/parser-0.0.1-SNAPSHOT.jar --rule <name> --input <source>
```

### Options

| Option | Description |
| --- | --- |
| `-i`, `--input` | A file path, a URL, inline text, or `-` for stdin. |
| `-r`, `--rule` | Name of the rule to apply (see `--list-rules`). |
| `-f`, `--format` | Force the format: `json`, `xml`, `html` (default: auto-detect). |
| `--list-rules` | List configured rules and their selectors, then exit. |
| `-q`, `--quiet` | Compact (single-line) JSON output. |
| `-h`, `--help` | Usage help. |

### Input sources

```bash
# file
java -jar parser.jar --rule json-user --input user.json
# URL (fetched over HTTP/HTTPS)
java -jar parser.jar --rule html-page --input https://example.com/page.html
# inline text (auto-detected as a path if it isn't, so quote it)
java -jar parser.jar --rule json-user --input '{"user":{"name":"Alice"}}'
# stdin
cat order.xml | java -jar parser.jar --rule xml-order --input -
```

### Exit codes

- `0` success
- `1` unexpected error (e.g. fetch failure)
- `2` parse/config error (unknown rule, undetectable format, invalid input)

## Defining rules

Rules live under `parser.rules` in `src/main/resources/application.yml`. Each
rule names a format (or leaves it as auto-detect) and maps **field names** to
**selectors** with a **mode**.

```yaml
parser:
  rules:
    json-user:
      format: JSON
      fields:
        name:   $.user.name          # plain string → VALUE mode (first match)
        emails: $.user.emails        #
        user:
          path: $.user               # object form lets you set the mode
          mode: SUBTREE              # extract the whole node

    xml-order:
      format: XML
      fields:
        id:      /*/@id              # an attribute
        status:  //order/status
        items:
          path: //order/items/item
          mode: LIST                 # all matches → a JSON array
        order:
          path: /order
          mode: SUBTREE              # the whole element as a nested object

    html-page:
      format: HTML
      fields:
        title:   title               # CSS selector
        links:
          path: a
          mode: LIST
        main:
          path: main
          mode: SUBTREE              # inner HTML
```

### Selector syntax by format

- **JSON** — [JSONPath](https://github.com/json-path/JsonPath): `$.user.name`,
  `$.orders[*].id`, `$.orders.length()`, `..name` (recursive), etc.
- **XML** — [XPath](https://www.w3.org/TR/1999/REC-xpath-19991116): `//order/status`,
  `/*/@id` (attributes), `//items/item` (all matches).
- **HTML** — [CSS selectors](https://jsoup.org/apidocs/org/jsoup/css/Selector.html):
  `h1`, `.box`, `a[href]`, `#main p.lead`, etc.

### Modes

| Mode | Meaning |
| --- | --- |
| `VALUE` | (default) First match — a scalar or a single node. |
| `LIST` | All matches — always returned as a JSON array. |
| `SUBTREE` | The whole matched node/object/array, serialized to JSON. |

A plain string selector is shorthand for `mode: VALUE`.

## Output

Everything is emitted as a single JSON object on stdout:

```json
{
  "format": "JSON",
  "source": "user.json",
  "fields": {
    "name": "Alice",
    "emails": ["alice@example.com", "alice@work.com"],
    "user": { "name": "Alice", "email": "alice@example.com", "emails": [...], "age": 30 }
  }
}
```

XML subtree/leaf values are mapped to JSON: attributes as `@attr`, leaf text as
`_text`, repeated children as arrays, empty elements as `""`.

## Notes

- HTML URLs are fetched over HTTP/HTTPS; XML and JSON come from files, URLs,
  inline text, or stdin.
- XML external entities are disabled and the parser runs in a defensive
  (non-network, non-XXE) configuration.
