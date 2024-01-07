package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"io/ioutil"
	"log"
	"net/http"
)

const gptModel = "YOUR-MODEL-NAME-HERE"
const apiKey = "YOUR-APIKEY-HERE"
const apiURL = "YOUR-URL-HERE" + "/completions"

var preKnowledge string

const postKnowledge = "\n\n此处只是数据操作的示例，不涉及返回值检查和异常处理。具体代码需要您在使用环境内自行调整。请访问 https://github.com/Sikongdddl/2023_SE4353_tiger 以获取完整用户文档。"

type GptResponse struct {
	ID      string `json:"id"`
	Object  string `json:"object"`
	Created int    `json:"created"`
	Model   string `json:"model"`
	Choices []struct {
		Index   int `json:"index"`
		Message struct {
			Role    string `json:"role"`
			Content string `json:"content"`
		} `json:"message"`
		Logprobs     any    `json:"logprobs"`
		FinishReason string `json:"finish_reason"`
	} `json:"choices"`
	Usage struct {
		PromptTokens     int `json:"prompt_tokens"`
		CompletionTokens int `json:"completion_tokens"`
		TotalTokens      int `json:"total_tokens"`
	} `json:"usage"`
	SystemFingerprint string `json:"system_fingerprint"`
}

type GptRequestBodyMessage struct {
	Role    string `json:"role"`
	Content string `json:"content"`
}

type GptRequestBody struct {
	Model    string                  `json:"model"`
	Messages []GptRequestBodyMessage `json:"messages"`
}

func postExecute(input string) string {
	return input + postKnowledge
}

// Save the context for continuous sessions
var gptContext []GptRequestBodyMessage

func main() {

	var preFileName = "preKnowledge.txt"

	content, err := ioutil.ReadFile(preFileName)
	if err != nil {
		log.Fatal(err)
	}
	preKnowledge = string(content)

	var gptSystemMessage GptRequestBodyMessage
	gptSystemMessage.Role = "system"
	gptSystemMessage.Content = preKnowledge
	gptContext = append(gptContext, gptSystemMessage)

	r := gin.Default()

	// Use CORS middleware
	config := cors.DefaultConfig()
	config.AllowOrigins = []string{"*"} // Allow all origins
	r.Use(cors.New(config))

	r.POST("/ping", func(c *gin.Context) {
		prompt := c.Query("prompt")
		gptAnswer := upper(prompt)
		c.JSON(200, gin.H{
			"message":  "pong",
			"response": postExecute(gptAnswer),
		})
	})

	r.DELETE("/clear", func(c *gin.Context) {
		var newGptContext []GptRequestBodyMessage
		gptContext = newGptContext
		gptContext = append(gptContext, gptSystemMessage)
	})

	go func() {
		if err := r.Run(":8080"); err != nil {
			fmt.Println("Error starting Gin server:", err)
		}
	}()

	// Start a simple HTTP server
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		http.ServeFile(w, r, "page.html")
	})

	// Start HTTP server in a goroutine
	go func() {
		if err := http.ListenAndServe(":8000", nil); err != nil {
			fmt.Println("Error starting HTTP server:", err)
		}
	}()

	// Keep the main goroutine running
	select {}
}

func upper(prompt string) string {

	var gptRequestBody GptRequestBody
	gptRequestBody.Model = gptModel

	var gptUserMessage GptRequestBodyMessage
	gptUserMessage.Role = "user"
	gptUserMessage.Content = prompt

	gptContext = append(gptContext, gptUserMessage)

	gptRequestBody.Messages = gptContext

	payload, err := json.Marshal(gptRequestBody)

	headers := map[string]string{
		"Authorization": "Bearer " + apiKey,
		"Content-Type":  "application/json",
	}

	response, err := sendPostRequest(apiURL, string(payload), headers)
	if err != nil {
		fmt.Println("Error:", err)
		return "error!"
	}

	var gptResponse GptResponse
	err = json.Unmarshal(response, &gptResponse)

	if err != nil {
		fmt.Println("Error decoding JSON:", err)
		return "error!"
	}

	var gptAssistantMessage GptRequestBodyMessage
	gptAssistantMessage.Role = "assistant"
	gptAssistantMessage.Content = gptResponse.Choices[0].Message.Content

	gptContext = append(gptContext, gptAssistantMessage)

	return gptResponse.Choices[0].Message.Content
}

func sendPostRequest(url, jsonData string, headers map[string]string) ([]byte, error) {

	req, err := http.NewRequest("POST", url, bytes.NewBuffer([]byte(jsonData)))
	if err != nil {
		return nil, err
	}
	for key, value := range headers {
		req.Header.Set(key, value)
	}

	client := &http.Client{}

	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	return body, nil
}
