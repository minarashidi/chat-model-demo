package dev.mina.chatmodeldemo;

import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.ai.parser.ListOutputParser;
import org.springframework.ai.parser.MapOutputParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StoryController {

  private final ChatClient chatClient;
  @Value("classpath:/prompts/story.st")
  private Resource storyPromptResource;

  public StoryController(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  @GetMapping("/story")
  public String generateStory(@RequestParam(value = "message", defaultValue = "Tell me a short funny story") String message) {
    return chatClient.call(message);
  }

  @GetMapping("/stories/must-read")
  public String getMustReadStories(@RequestParam(value = "genre", defaultValue = "bedtime") String genre) {
    PromptTemplate promptTemplate = new PromptTemplate(storyPromptResource);
    Prompt prompt = promptTemplate.create(Map.of("genre", genre));
    return chatClient.call(prompt).getResult().getOutput().getContent();
  }

  @GetMapping("/stories/popular")
  public List<String> getPopularStories(@RequestParam(value = "genre", defaultValue = "Science Fiction") String genre) {
    var message = """
        Give me a list of 10 most popular stories for the genre: {genre}. If you don't know the answer, just say "I don't know" {format}
        """;
    ListOutputParser outputParser = new ListOutputParser(new DefaultConversionService());

    PromptTemplate promptTemplate = new PromptTemplate(message, Map.of("genre", genre, "format", outputParser.getFormat()));
    Prompt prompt = promptTemplate.create();

    ChatResponse response = chatClient.call(prompt);
    return outputParser.parse(response.getResult().getOutput().getContent());
  }

  @GetMapping("/stories/author")
  public Map<String, Object> getAuthorsStories(@RequestParam(value = "author", defaultValue = "J.K Rowling") String author) {
    var message = """
        Give me a list of stories for the author {author}, including their genres. If you don't know the answer, just say "I don't know" {format}
        """;
    MapOutputParser outputParser = new MapOutputParser();

    PromptTemplate promptTemplate = new PromptTemplate(message, Map.of("author", author, "format", outputParser.getFormat()));
    Prompt prompt = promptTemplate.create();

    ChatResponse response = chatClient.call(prompt);
    return outputParser.parse(response.getResult().getOutput().getContent());
  }

  @GetMapping("/stories/random-by-author")
  public Story getRandomStoryByAuthor(@RequestParam(value = "author", defaultValue = "J.K Rowling") String author) {
    var message = """
        Give me a story for the author {author}. If you don't know the answer, just say "I don't know" {format}
        """;
    var outputParser = new BeanOutputParser<>(Story.class);

    PromptTemplate promptTemplate = new PromptTemplate(message, Map.of("author", author, "format", outputParser.getFormat()));
    Prompt prompt = promptTemplate.create();

    ChatResponse response = chatClient.call(prompt);
    return outputParser.parse(response.getResult().getOutput().getContent());
  }

}
