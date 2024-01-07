First choose GPT mirror site and model, then put your apiKey in.

```Golang
// backend.go     lines 14 - 16
const gptModel = "YOUR-MODEL-NAME-HERE"
const apiKey = "YOUR-APIKEY-HERE"
const apiURL = "YOUR-URL-HERE" + "/completions"
```

Then, run the go code:

```
go run backend.go
```

The frontend chatbot page will be served on port ```:8000```, and the backend will run on port ```:8080```. So ensure that the two ports are not occupied before you run the code.

There will be some console logs and warnings because the code runs in debug mode by default, but it doesn't matter.

Then you can visit ```http://localhost:8000``` to begin your dialogue.

NOTES: Dialogue context will not be automatically cleared. You must explicitly press the clear button in the frontend or restart the backend to clear the context. One frontend session at a time, there is no promise how the code will work if multiple frontend page is opened.