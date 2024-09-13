package in.tech_camp.pictweet;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/tweets")
@AllArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;

    @PostMapping("/{tweetId}/comment")
    public String postComment(@ModelAttribute("commentForm") @Validated(GroupOrder.class) CommentForm commentForm,
                              BindingResult result,
                              Authentication authentication,
                              @PathVariable("tweetId") Integer tweetId,
                              Model model)
    {
        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email);
        TweetEntity tweet;
        tweet = tweetRepository.findById(tweetId)
                    .orElseThrow(() -> new EntityNotFoundException("ツイートが見つかりませんでした。"));

        // バリデーションエラーがあるかチェックする
        if (result.hasErrors()) {
            model.addAttribute("errorMessages", result.getAllErrors());
            model.addAttribute("tweet", tweet);
            model.addAttribute("comments", commentRepository.findByTweet_id(tweetId));
            model.addAttribute("commentForm", commentForm);
            return "tweets/detail";
        }

        // バリデーションエラーがなければ、コメントを保存する
        CommentEntity comment = new CommentEntity();
        comment.setText(commentForm.getText());
        comment.setTweet(tweet);
        comment.setUser(user);
        commentRepository.save(comment);

        // 成功時には詳細画面にリダイレクトする
        return "redirect:/tweets/" + tweetId;
    }
}
