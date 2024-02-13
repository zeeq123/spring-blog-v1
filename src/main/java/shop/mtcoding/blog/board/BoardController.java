package shop.mtcoding.blog.board;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import shop.mtcoding.blog._core.config.security.MyLoginUser;
import shop.mtcoding.blog.user.User;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class BoardController {

    private final BoardRepository boardRepository;
    private final HttpSession session;

    @PostMapping("/board/{id}/update")
    public String update(@PathVariable int id, BoardRequest.UpdateDTO requestDTO){
        // 1. 인증 체크
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. 권한 체크
        Board board = boardRepository.findById(id);
        if (board.getUserId() != sessionUser.getId()){
            return "error/403";
        }

        // 3. 핵심 로직
        // update board_tb set title = ?, content=? where id = ?;
        boardRepository.update(requestDTO, id);

        return "redirect:/board/" + id;
    }

    @GetMapping("/board/{id}/updateForm")
    public String updateForm(@PathVariable int id, HttpServletRequest request){
        // 1. 인증 안되면 내보내기
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. 권한 없으면 내보내기
        // 모델 위임(id로 board 조회)
        Board board = boardRepository.findById(id);
        if (board.getUserId() != sessionUser.getId()){
            return "error/403";
        }

        // 3. 가방에 담기
        request.setAttribute("board", board);

        return "board/updateForm";
    }

    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable int id, HttpServletRequest request){
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. 권한 없으면 내보내기
        Board board = boardRepository.findById(id);
        if (board.getUserId() != sessionUser.getId()){
            request.setAttribute("status", 403);
            request.setAttribute("msg", "게시글을 삭제할 권한이 없습니다.");
            return "error/40x";
        }

        boardRepository.deleteById(id);

        return "redirect:/";
    }

    @PostMapping("/board/save")
    public String save(BoardRequest.SaveDTO requestDTO, HttpServletRequest request){
        // 1. 인증 체크
        User sessionUser = (User) session.getAttribute("sessionUser");
        // 2. 바디 데이터 확인 및 유효성 검사
        System.out.println(requestDTO);

        if (requestDTO.getTitle().length() > 30){
            request.setAttribute("status", 400);
            request.setAttribute("msg", " title의 길이가 30자를 초과해서는 안됩니다.");
            return "error/40x"; // BadRequest
        }

        // 3. 모델 위임
        // insert into board_tb(title, content, user_id, created_at) values(?, ?, ?, now())
        boardRepository.save(requestDTO, sessionUser.getId());

        return "redirect:/";
    }

    @GetMapping("/")
    public String index(HttpServletRequest request, @AuthenticationPrincipal MyLoginUser myLoginUser) {

        System.out.println("로그인 : " + myLoginUser.getUsername());
        List<Board> boardList = boardRepository.findAll();
        request.setAttribute("boardList", boardList);

        return "index";
    }

    @GetMapping("/board/saveForm")
    public String saveForm(HttpServletRequest request) {

        HttpSession session = request.getSession();

        User sessionUser = (User) session.getAttribute("sessionUser");

        if (sessionUser == null){
            return "redirect:/loginForm";
        }
        return "board/saveForm";
    }

    @GetMapping("/board/{id}")
    public String detail(@PathVariable int id, HttpServletRequest request) {
        // 1. 모델 진입 = 상세보기 데이터 가져오기
        BoardResponse.DetailDTO responseDTO = boardRepository.findByIdWithUser(id);

        // 2. 페이지 주인 여부 체크 (board의 userId와 sessionUser
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 3. 로그인을 안했을 때
        boolean pageOwner;
        if (sessionUser == null){
            pageOwner = false;
        }else{
            int boardUserId = responseDTO.getUserId();
            int userId = sessionUser.getId();
            pageOwner = boardUserId == userId;
        }

        request.setAttribute("board", responseDTO);
        request.setAttribute("pageOwner", pageOwner);
        return "board/detail";
    }
}
