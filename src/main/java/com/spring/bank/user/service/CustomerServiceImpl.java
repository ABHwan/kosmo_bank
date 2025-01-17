package com.spring.bank.user.service;

import java.sql.Date;
//import java.util.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.spring.bank.customer.encrypt.UserAuthenticationService;
import com.spring.bank.product.vo.DepositProductVO;
import com.spring.bank.product.vo.FundProductVO;
import com.spring.bank.product.vo.IrpProductVO;
import com.spring.bank.product.vo.SavingProductVO;
import com.spring.bank.user.dao.CustomerDAOImpl;
import com.spring.bank.user.vo.AccountBookVO;
import com.spring.bank.user.vo.AccountVO;
import com.spring.bank.user.vo.AutoTransferVO;
import com.spring.bank.user.vo.CrawlerVO;
import com.spring.bank.user.vo.DepositVO;
import com.spring.bank.user.vo.InquiryVO;
import com.spring.bank.user.vo.IrpVO;
import com.spring.bank.user.vo.LoanHistoryVO;
import com.spring.bank.user.vo.LoanProductVO;
import com.spring.bank.user.vo.LoanVO;
import com.spring.bank.user.vo.MyDepositVO;
import com.spring.bank.user.vo.MyIRPVO;
import com.spring.bank.user.vo.MySavingVO;
import com.spring.bank.user.vo.NoticeVO;
import com.spring.bank.user.vo.TransferVO;
import com.spring.bank.user.vo.UserVO;
import com.spring.bank.user.vo.faqVO;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerDAOImpl dao;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	SqlSessionTemplate sqlSession;

	@Autowired
	JavaMailSender mailSender;
	
	// 로그인 시 index 에 계좌 불러오기
	@Override
	public void accountLoad(HttpServletRequest req, Model model) {
		System.out.println("[서비스 => 계좌불러오기]");

		String member_id = (String) req.getSession().getAttribute("customerID");

		// 고유키를 통해 해당하는 연동된 계좌들을 불러온다.
		List<AccountVO> dtos = dao.getAccountList(member_id);

		// 대표계좌 불러오기
		AccountVO vo = dao.getAccountDefault(member_id);

		req.setAttribute("vo", vo);
		req.setAttribute("dtos", dtos);

	}

	// 아이디 중복확인
	@Override
	public int confirmIdAction(Map<String, Object> map) {

		System.out.println("[서비스 => ID 중복확인 처리]");

		System.out.println(map.get("member_id"));

		return dao.idCheck(map);
	}

	// 명의 중복확인
	@Override
	public int duplicateAction(Map<String, Object> map) {

		System.out.println("[서비스 => 명의 중복확인 처리]");

		System.out.println(map.get("unique_key"));

		return dao.duplicateCheck(map);
	}

	// 회원가입 처리
	@Override
	public void registerAction(HttpServletRequest req, Model model) {
		System.out.println("[서비스 => 회원가입 처리]");

		String unique_key = req.getParameter("unique_key");

		System.out.println("uk : " + unique_key);

		// 3단계. 화면으로부터 입력 받은 값을 받아온다. 바구니에 담는다.
		UserVO vo = new UserVO();

		String strPassword = bCryptPasswordEncoder.encode(req.getParameter("password"));

		String hp = req.getParameter("hp");

		String email = "";
		String email1 = req.getParameter("email1");
		String email2 = req.getParameter("email2");

		email = email1 + "@" + email2;

		String zipcode = req.getParameter("address_zipcode");

		vo.setMember_id(req.getParameter("id"));
		vo.setMember_password(strPassword);
		vo.setMember_name(req.getParameter("name"));
		vo.setMember_birth(Date.valueOf(req.getParameter("birth")));
		vo.setMember_hp(hp);
		vo.setMember_email(email);
		vo.setMember_zipcode(Integer.parseInt(zipcode));
		vo.setMember_addr1(req.getParameter("address_addr1"));
		vo.setMember_addr2(req.getParameter("address_addr2"));
		vo.setMember_addr3(req.getParameter("address_addr3"));
		vo.setUnique_key(req.getParameter("unique_key"));

		// regDate는 입력값이 없으면 defalut가 sysdate

		// 5단계. 회원가입 처리
		int insertCnt = dao.insertUser(vo);
		System.out.println("insertCnt : " + insertCnt);

		// 6단계. jsp로 결과 전달(request나 session으로 처리 결과를 저장 후)
		req.setAttribute("insertCnt", insertCnt);
	}

	@Override
	public void deleteCustomerAction(HttpServletRequest req, Model model) {
		System.out.println("[서비스 => 회원탈퇴 처리]");

		// 3단계. 화면으로부터 입력 받은 값을 가져오기
		String id = (String) req.getSession().getAttribute("customerID");

		// 4단계. 싱글톤 방식으로 dao 객체 생성, 다형성 적용
		// 5-1단계. 회원탈퇴 인증 처리
		int deleteCnt = 0;

		// 5-2단계. 인증성공 시 탈퇴처리
		if (id != null) {
			deleteCnt = dao.deleteUser(id);
			System.out.println("deleteCnt : " + deleteCnt);
		}

		// 6단계. jsp로 결과 전달(request나 session으로 처리 결과를 저장 후)
		req.setAttribute("deleteCnt", deleteCnt);
	}

	@Override
	public void modifyDetailAction(HttpServletRequest req, Model model) {

		System.out.println("[서비스 => 회원수정 인증 및 상세화면]");

		String id = (String) req.getSession().getAttribute("customerID");

		UserAuthenticationService confirm = new UserAuthenticationService(sqlSession);

		// 3단계. 화면으로부터 입력 받은 값을 가져오기

		System.out.println("세션 아이디 : " + id);
		String password = req.getParameter("password");

		String ecPassword = confirm.loadUserByUsername(id).getPassword();

		String encodePassword = ecPassword.replace("{bcrypt}", "");

		boolean chk = bCryptPasswordEncoder.matches(password, encodePassword);

		System.out.println(chk);

		System.out.println("password : " + password);
		System.out.println("ecPassword : " + ecPassword);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("password", encodePassword);

		// 5-1단계. 회원수정 인증 처리
		int idPwdChkCnt = dao.idPasswordChk(map);

		// 5-2단계. 인증성공 시 상세 정보 조회
		UserVO vo = null;
		if (chk) {
			vo = dao.getUserInfo(id);
			System.out.println("타니?");
		}

		System.out.println("vo : " + vo.getMember_name());
		System.out.println("vo : " + vo.getMember_birth());
		System.out.println("vo : " + vo.getMember_id());
		System.out.println("vo : " + vo.getMember_hp());
		System.out.println("vo : " + vo.getMember_email());
		System.out.println("vo : " + vo.getMember_zipcode());
		System.out.println("vo : " + vo.getMember_addr1());
		System.out.println("vo : " + vo.getMember_addr2());
		System.out.println("vo : " + vo.getMember_addr3());
		System.out.println("vo : " + vo.getMember_indate());

		System.out.println("idPwdChkCnt : " + idPwdChkCnt);

		// 6단계. jsp로 결과 전달(request나 session으로 처리 결과를 저장 후)
		req.setAttribute("selectCnt", idPwdChkCnt);
		req.setAttribute("vo", vo);

	}

	@Override
	public void modifyCustomerAction(HttpServletRequest req, Model model) {
		System.out.println("[서비스 => 회원수정 처리]");

		// 3단계. 화면으로부터 입력 받은 값을 가져오기
		String strId = (String) req.getSession().getAttribute("customerID");
		String strPassword = req.getParameter("password");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", strId);
		map.put("password", strPassword);

		// 4단계. 싱글톤 방식으로 dao 객체 생성, 다형성 적용
		int idPwdCheck = dao.idPasswordChk(map);

		// id, password, name, hp, email
		UserVO vo = new UserVO();

		String hp = "";
		String hp1 = req.getParameter("hp1");
		String hp2 = req.getParameter("hp2");
		String hp3 = req.getParameter("hp3");
		// hp가 필수가 아니므로 null 값이 들어올 수 있으므로 값이 존재할 때만 처리
		if (!hp1.equals("") && !hp2.equals("") && !hp3.equals("")) {
			hp = hp1 + "-" + hp2 + "-" + hp3;
		}

		String email1 = req.getParameter("email1");
		String email2 = req.getParameter("email2");
		String email = email1 + "@" + email2;

		String password = "";
		String passwordChange = req.getParameter("password_change");
		String enPasswordChange = bCryptPasswordEncoder.encode(passwordChange);

		if (idPwdCheck == 1) {
			// 비밀번호 변경 값이 존재하지 않을 때
			if (passwordChange == "") {
				// 기존 비밀번호 유지
				password = req.getParameter("password");

				// 비밀번호 변경 값이 존재할 때
			} else {
				// 비밀번호 변경 값으로 변경
				password = enPasswordChange;
			}
		}

		vo.setMember_name(req.getParameter("name"));
		vo.setMember_birth(Date.valueOf(req.getParameter("birth")));
		vo.setMember_id((String) req.getSession().getAttribute("customerID"));
		vo.setMember_password(password);
		vo.setMember_hp(hp);
		vo.setMember_email(email);
		vo.setMember_zipcode(Integer.parseInt(req.getParameter("address_zipcode")));
		vo.setMember_addr1(req.getParameter("address_addr1"));
		vo.setMember_addr2(req.getParameter("address_addr2"));
		vo.setMember_addr3(req.getParameter("address_addr3"));

		// 5단계. 회원수정 인증 처리
		int updateCnt = dao.updateUser(vo);
		System.out.println("updateCnt : " + updateCnt);

		// 6단계. jsp로 결과 전달(request나 session으로 처리 결과를 저장 후)
		req.setAttribute("updateCnt", updateCnt);
	}

	// 회원 인증 화면
	@Override
	public void confirmAction(HttpServletRequest req, Model model) {

		System.out.println("[서비스 => 회원 인증 화면]");

		// 3단계. 화면으로부터 입력 받은 값을 가져오기
		String strId = (String) req.getSession().getAttribute("customerID");
		String strPassword = req.getParameter("password");

		System.out.println("strId : " + strId);
		System.out.println("strPassword : " + strPassword);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", strId);
		map.put("password", strPassword);

		// 4단계. 싱글톤 방식으로 dao 객체 생성, 다형성 적용
		int idPwdChkCnt = dao.idPasswordChk(map);

		System.out.println("idPwdChkCnt : " + idPwdChkCnt);
		// 6단계. jsp로 결과 전달(request나 session으로 처리 결과를 저장 후)
		req.setAttribute("selectCnt", idPwdChkCnt);

	}

	// id 찾기
	@Override
	public void idFindAction(HttpServletRequest req, Model model) {
		String member_name = req.getParameter("member_name");
		String member_email = req.getParameter("member_email");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("member_name", member_name);
		map.put("member_email", member_email);

		// id 찾기 처리
		UserVO vo = dao.idFind(map);
		if (vo != null) {
			System.out.println("찾은 id : " + vo.getMember_id());
		}

		// jsp로 결과 전달
		model.addAttribute("vo", vo);
	}

	// 임시비밀번호로 변경하고 이메일 전송
	@Override
	public void sendEmail(Map<String, Object> map) {
		try {

			MimeMessage message = mailSender.createMimeMessage();
			String txt = "KOSMO BANK 임시비밀번호 전송메일입니다. <br/>" + "임시비밀번호 : " + (String) map.get("member_password")
					+ "<br/> 해당 비밀번호로 로그인 하시고 비밀번호 변경해주세요~!";
			message.setSubject("KOSMO BANK 임시비밀번호 전송메일입니다");
			message.setText(txt, "UTF-8", "html");
			message.setFrom(new InternetAddress("xkrrhsdl7@gmail.com")); // 보내는사람
			message.addRecipient(RecipientType.TO, new InternetAddress((String) map.get("member_email"))); // 받는사람
			mailSender.send(message);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 임시비밀번호로 재설정
	@Override
	public void pwFindAction(HttpServletRequest req, Model model) {
		// 입력한 정보로 멤버 정보 구해오기
		String member_name = req.getParameter("member_name");
		String member_email = req.getParameter("member_email");
		String member_id = req.getParameter("member_id");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("member_name", member_name);
		map.put("member_email", member_email);
		map.put("member_id", member_id);

		UserVO vo = dao.customerFind(map);
		System.out.print("vo : " + vo);
		int updateCnt = 0;

		if (vo == null) {
			System.out.println("입력하신 정보로 회원정보를 찾을 수 없습니다.");
		} else {
			// 임시 비밀번호 생성
			String pw = "";
			for (int i = 0; i < 12; i++) {
				pw += (char) ((Math.random() * 26) + 97);
			}
			// 메일은 임시비밀번호 자체를 보내고
			Map<String, Object> sendMap = new HashMap<String, Object>();
			sendMap.put("member_id", vo.getMember_id());
			sendMap.put("member_password", pw);
			sendMap.put("member_email", member_email);
			System.out.println("설정한 임시비밀번호 : " + pw);

			// 저장은 임시비밀번호를 암호화해서 저장한다.
			vo.setMember_password(bCryptPasswordEncoder.encode(pw));

			// 비밀번호 변경
			updateCnt = dao.updatePassword(vo);

			// 비밀번호 변경 메일 발송(아이디와 암호화되기전 비밀번호를 보낸다)
			sendEmail(sendMap);

		}
		System.out.println("updateCnt: " + updateCnt);
		model.addAttribute("updateCnt", updateCnt);
	}

	// 문의내역 List
	@Override
	public void inquiryList(HttpServletRequest req, Model model) {
		// 3단계. 화면으로부터 입력받은 값을 받아온다.
		// 페이징
		int pageSize = 5; // 한페이지당 출력할 글 갯수
		int pageBlock = 3; // 한 블록당 페이지 갯수

		int cnt = 0; // 글 갯수
		int start = 0; // 현재페이지 시작 글 번호
		int end = 0; // 현재페이지 마지막 글 번호
		int number = 0; // 출력용 글번호
		String pageNum = ""; // 페이지 번호
		int currentPage = 0; // 현재 페이지

		int pageCount = 0; // 페이지 갯수
		int startPage = 0; // 시작페이지
		int endPage = 0; // 마지막페이지

		// 5-1단계. 게시글 갯수 조회
		cnt = dao.getInquiryCnt();

		System.out.println("cnt ==> " + cnt);

		// 5-2단계. 게시글 목록 조회
		pageNum = req.getParameter("pageNum");

		if (pageNum == null) {
			pageNum = "1"; // 첫페이지를 1페이지로 지정
		}

		// 글 30건 기준
		currentPage = Integer.parseInt(pageNum);
		System.out.println("currentPage : " + currentPage);

		// 페이지 갯수 6 = (30/5) + (0)
		pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지 있으면 1페이지

		// 현재페이지 시작 글번호(페이지별)
		// start = (currentPage - 1) * pageSize +1;
		// 1 = (1 - 1 )* 5 + 1
		start = (currentPage - 1) * pageSize + 1;

		// 현재페이지 마지막 글번호(페이지별)
		// end = start + pageSize - 1;
		// 5 = 1 + 5 - 1
		end = start + pageSize - 1;

		System.out.println("start : " + start);
		System.out.println("end : " + end);

		// 출력용 글번호
		// 30 = 30 - (1 - 1) * 5 //1페이지
		// number = cnt- (currentPage - 1) * pageSize;
		number = cnt - (currentPage - 1) * pageSize;

		System.out.println("number : " + number);
		System.out.println("pageSize : " + pageSize);

		// 시작페이지
		// 1 = (1 / 3) * 3 + 1;
		// startPage = (currentPage / pageBlock) * pageBlock + 1;
		startPage = (currentPage / pageBlock) * pageBlock + 1;
		if (currentPage % pageBlock == 0)
			startPage -= pageBlock;

		System.out.println("startPage : " + startPage);

		// 마지막 페이지
		// 3 = 1 + 3 - 1
		endPage = startPage + pageBlock - 1;
		if (endPage > pageCount)
			endPage = pageCount;

		System.out.println("endPage : " + endPage);

		System.out.println("--------------------------");

		List<InquiryVO> dtos = null;

		if (cnt > 0) {
			// 5-2단계. 게시글 목록 조회
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("start", start);
			map.put("end", end);
			dtos = dao.getInquiryList(map);

		}

		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("dtos", dtos); // 게시글 목록
		req.setAttribute("cnt", cnt); // 글개수
		req.setAttribute("pageNum", pageNum); // 페이지 번호
		req.setAttribute("number", number); // 출력용 글번호

		if (cnt > 0) {
			req.setAttribute("startPage", startPage); // 시작페이지
			req.setAttribute("endPage", endPage); // 마지막 페이지
			req.setAttribute("pageBlock", pageBlock); // 한블럭당 페이지 갯수
			req.setAttribute("pageCount", pageCount); // 페이지 갯수
			req.setAttribute("currentPage", currentPage); // 현재페이지
		}

	}

	// QNA 글쓰기 처리
	@Override
	public void inquiryWriteAction(HttpServletRequest req, Model model) {
		int insertCnt = 0;

		InquiryVO vo = new InquiryVO();

		// 3-1단계. 화면으로부터 입력받은 값(hidden값)을 받아온다.
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));

		// 3-2단계. 화면으로부터 입력받은 값(input 값 = 작성자, 비밀번호, 글제목, 글내용)을 받아와서 바구니에 담는다
		vo.setMember_id(req.getParameter("customerID"));
		vo.setInquiry_title(req.getParameter("inquiry_title"));
		vo.setInquiry_content(req.getParameter("inquiry_content"));

		// 3-3단계. 작성일, IP
		vo.setInquiry_regDate(new Timestamp(System.currentTimeMillis()));
		// 화면실행시 url의 localhost 대신에 본인 IP를 넣으면 그 ip가 db에 insert된다.
		// 예)http://본인 ip/jsp_mvcQna_jjh/QnaList.bo
		/* vo.setIp(req.getRemoteAddr()); */

		// 5단계. 게시글 작성
		insertCnt = dao.insertInquiry(vo);
		System.out.println("insertCnt : " + insertCnt);

		// 6단계
		req.setAttribute("insertCnt", insertCnt);
		req.setAttribute("pageNum", pageNum);

	}

	// qna 상세보기 페이지
	@Override
	public void InquiryDetailAction(HttpServletRequest req, Model model) {
		// 3단계. 화면으로부터 입력받은 값을 받아온다.
		// http://localhost/jsp_mvcBoard_jjh/boardDetail.bo?=num=30&pageNum=1&number=30
		int inquiry_id = Integer.parseInt(req.getParameter("inquiry_id"));
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int number = Integer.parseInt(req.getParameter("number"));

		// 5-1단계. 조회수 증가
		// addReadCnt
		dao.addReadCnt(inquiry_id);

		// 5-2단계. 게시글 상세페이지 조회
		// getQnaDetail
		InquiryVO vo = dao.getQnaDetail(inquiry_id);
		System.out.println("왜안나오는데 => " + vo.getInquiry_answer());
		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("dto", vo);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("number", number);
	}

	// qna 수정
	@Override
	public void InquiryModifyDetailAction(HttpServletRequest req, Model model) {

		int inquiry_id = Integer.parseInt(req.getParameter("inquiry_id"));
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));

		// 5-2 단계. 상세페이지 조회
		InquiryVO vo = dao.getQnaDetail(inquiry_id);

		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("dto", vo);
		req.setAttribute("inquiry_id", inquiry_id);
		req.setAttribute("pageNum", pageNum);

	}

	// qna 수정 처리
	@Override
	public void inquiryModifyAction(HttpServletRequest req, Model model) {

		System.out.println(req.getParameter("inquiry_id"));
		int inquiry_id = Integer.parseInt(req.getParameter("inquiry_id"));
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));

		// QnaVO바구니 생성
		// 화면으로부터 입력받은 값(input값 - 작성자,비밀번호, 제목, 내용), num을 받아온다.
		InquiryVO vo = new InquiryVO();
		vo.setMember_id(req.getParameter("customerID"));
		vo.setInquiry_title(req.getParameter("inquiry_title"));
		vo.setInquiry_content(req.getParameter("inquiry_content"));
		vo.setInquiry_id(inquiry_id);

		// 5단계. 게시글 수정처리
		int updateCnt = dao.updateQna(vo);
		System.out.println("updateCnt : " + updateCnt);

		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("updateCnt", updateCnt);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("inquiry_id", inquiry_id);
	}

	// QNA 수정, 삭제 할때 비밀번호 확인
	@Override
	public void QnaPasswordConfirm(HttpServletRequest req, Model model) {

		// 3단계. 화면으로부터 입력받은 값(input값)을 받아온다.
		String id = (String) req.getSession().getAttribute("customerID");
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int inquiry_id = Integer.parseInt(req.getParameter("inquiry_id"));
		UserAuthenticationService confirm = new UserAuthenticationService(sqlSession);

		// 3단계. 화면으로부터 입력 받은 값을 가져오기

		System.out.println("세션 아이디 : " + id);
		String inquiry_password = req.getParameter("inquiry_password");

		String ecPassword = confirm.loadUserByUsername(id).getPassword();

		String encodePassword = ecPassword.replace("{bcrypt}", "");

		boolean chk = bCryptPasswordEncoder.matches(inquiry_password, encodePassword);

		int selectCnt = 0;

		if (chk) {
			// QnaModify.bo?num=30&pageNum=1
			// hidden으로 넘어온 값(hidden 값) 받아온다.
			System.out.println(req.getParameter("inquiry_id"));
			// 5단계. 비밀번호 인증
			selectCnt = 1;
			System.out.println("qna 수정 , 삭제 시 비밀번호 확인 selectCnt = " + selectCnt);
		}
		model.addAttribute("selectCnt", selectCnt);
		model.addAttribute("inquiry_id", inquiry_id);
		model.addAttribute("pageNum", pageNum);

	}

	// qna 삭제 처리
	@Override
	public void inquiryDelete(HttpServletRequest req, Model model) {
		System.out.println("삭제처리 아이디 : " + req.getParameter("inquiry_id"));
		int inquiry_id = Integer.parseInt(req.getParameter("inquiry_id"));
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));

		// 5단계. 게시글 수정처리
		int deleteCnt = dao.deleteQna(inquiry_id);
		System.out.println("updateCnt : " + deleteCnt);

		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("deleteCnt", deleteCnt);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("inquiry_id", inquiry_id);
	}

	// faq 조회
	@Override
	public void faqList(HttpServletRequest req, Model model) {
		// 3단계. 화면으로부터 입력받은 값을 받아온다.
		// 페이징
		int pageSize = 8; // 한페이지당 출력할 글 갯수
		int pageBlock = 3; // 한 블록당 페이지 갯수

		int cnt = 0; // 글 갯수
		int start = 0; // 현재페이지 시작 글 번호
		int end = 0; // 현재페이지 마지막 글 번호
		int number = 0; // 출력용 글번호
		String pageNum = ""; // 페이지 번호
		int currentPage = 0; // 현재 페이지

		int pageCount = 0; // 페이지 갯수
		int startPage = 0; // 시작페이지
		int endPage = 0; // 마지막페이지

		// 5-1단계. 게시글 갯수 조회
		cnt = dao.getFaqCnt();

		System.out.println("cnt ==> " + cnt);

		// 5-2단계. 게시글 목록 조회
		pageNum = req.getParameter("pageNum");

		if (pageNum == null) {
			pageNum = "1"; // 첫페이지를 1페이지로 지정
		}

		// 글 30건 기준
		currentPage = Integer.parseInt(pageNum);
		System.out.println("currentPage : " + currentPage);

		// 페이지 갯수 6 = (30/5) + (0)
		pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지 있으면 1페이지

		// 현재페이지 시작 글번호(페이지별)
		// start = (currentPage - 1) * pageSize +1;
		// 1 = (1 - 1 )* 5 + 1
		start = (currentPage - 1) * pageSize + 1;

		// 현재페이지 마지막 글번호(페이지별)
		// end = start + pageSize - 1;
		// 5 = 1 + 5 - 1
		end = start + pageSize - 1;

		System.out.println("start : " + start);
		System.out.println("end : " + end);

		// 출력용 글번호
		// 30 = 30 - (1 - 1) * 5 //1페이지
		// number = cnt- (currentPage - 1) * pageSize;
		number = cnt - (currentPage - 1) * pageSize;

		System.out.println("number : " + number);
		System.out.println("pageSize : " + pageSize);

		// 시작페이지
		// 1 = (1 / 3) * 3 + 1;
		// startPage = (currentPage / pageBlock) * pageBlock + 1;
		startPage = (currentPage / pageBlock) * pageBlock + 1;
		if (currentPage % pageBlock == 0)
			startPage -= pageBlock;

		System.out.println("startPage : " + startPage);

		// 마지막 페이지
		// 3 = 1 + 3 - 1
		endPage = startPage + pageBlock - 1;
		if (endPage > pageCount)
			endPage = pageCount;

		System.out.println("endPage : " + endPage);

		System.out.println("--------------------------");

		List<faqVO> dtos = null;

		if (cnt > 0) {
			// 5-2단계. 게시글 목록 조회
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("start", start);
			map.put("end", end);
			dtos = dao.getFaqList(map);
		}

		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("dtos", dtos); // 게시글 목록
		req.setAttribute("cnt", cnt); // 글개수
		req.setAttribute("pageNum", pageNum); // 페이지 번호
		req.setAttribute("number", number); // 출력용 글번호

		if (cnt > 0) {
			req.setAttribute("startPage", startPage); // 시작페이지
			req.setAttribute("endPage", endPage); // 마지막 페이지
			req.setAttribute("pageBlock", pageBlock); // 한블럭당 페이지 갯수
			req.setAttribute("pageCount", pageCount); // 페이지 갯수
			req.setAttribute("currentPage", currentPage); // 현재페이지
		}

	}

	// 예금 상품 조회
	@Override
	public void depositList(HttpServletRequest req, Model model) {
		// 3단계. 화면으로부터 입력받은 값을 받아온다.
		// 페이징
		int pageSize = 8; // 한페이지당 출력할 글 갯수
		int pageBlock = 3; // 한 블록당 페이지 갯수

		int cnt = 0; // 글 갯수
		int start = 0; // 현재페이지 시작 글 번호
		int end = 0; // 현재페이지 마지막 글 번호
		int number = 0; // 출력용 글번호
		String pageNum = ""; // 페이지 번호
		int currentPage = 0; // 현재 페이지

		int pageCount = 0; // 페이지 갯수
		int startPage = 0; // 시작페이지
		int endPage = 0; // 마지막페이지

		// 5-1단계. 게시글 갯수 조회
		cnt = dao.getDepositCnt();

		System.out.println("cnt ==> " + cnt);

		// 5-2단계. 게시글 목록 조회
		pageNum = req.getParameter("pageNum");

		if (pageNum == null) {
			pageNum = "1"; // 첫페이지를 1페이지로 지정
		}

		// 글 30건 기준
		currentPage = Integer.parseInt(pageNum);
		System.out.println("currentPage : " + currentPage);

		// 페이지 갯수 6 = (30/5) + (0)
		pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지 있으면 1페이지

		// 현재페이지 시작 글번호(페이지별)
		// start = (currentPage - 1) * pageSize +1;
		// 1 = (1 - 1 )* 5 + 1
		start = (currentPage - 1) * pageSize + 1;

		// 현재페이지 마지막 글번호(페이지별)
		// end = start + pageSize - 1;
		// 5 = 1 + 5 - 1
		end = start + pageSize - 1;

		System.out.println("start : " + start);
		System.out.println("end : " + end);

		// 출력용 글번호
		// 30 = 30 - (1 - 1) * 5 //1페이지
		// number = cnt- (currentPage - 1) * pageSize;
		number = cnt - (currentPage - 1) * pageSize;

		System.out.println("number : " + number);
		System.out.println("pageSize : " + pageSize);

		// 시작페이지
		// 1 = (1 / 3) * 3 + 1;
		// startPage = (currentPage / pageBlock) * pageBlock + 1;
		startPage = (currentPage / pageBlock) * pageBlock + 1;
		if (currentPage % pageBlock == 0)
			startPage -= pageBlock;

		System.out.println("startPage : " + startPage);

		// 마지막 페이지
		// 3 = 1 + 3 - 1
		endPage = startPage + pageBlock - 1;
		if (endPage > pageCount)
			endPage = pageCount;

		System.out.println("endPage : " + endPage);

		System.out.println("--------------------------");

		List<DepositProductVO> dtos = null;

		if (cnt > 0) {
			// 5-2단계. 게시글 목록 조회

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("start", start);
			map.put("end", end);
			dtos = dao.getDepositList(map);
		}

		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("dtos", dtos); // 게시글 목록
		req.setAttribute("cnt", cnt); // 글개수
		req.setAttribute("pageNum", pageNum); // 페이지 번호
		req.setAttribute("number", number); // 출력용 글번호

		if (cnt > 0) {
			req.setAttribute("startPage", startPage); // 시작페이지
			req.setAttribute("endPage", endPage); // 마지막 페이지
			req.setAttribute("pageBlock", pageBlock); // 한블럭당 페이지 갯수
			req.setAttribute("pageCount", pageCount); // 페이지 갯수
			req.setAttribute("currentPage", currentPage); // 현재페이지
		}
	}

	// 예금 상품 검색
	@Override
	public void searchDepositProduct(HttpServletRequest req, Model model) {

		// 입력받은 검색어
		String search = req.getParameter("search");
		System.out.println("관리자 페이지 회원 검색어 : " + search);

		// 페이징
		int pageSize = 10; // 한 페이지당 출력할 예금상품
		int pageBlock = 3; // 한 블럭당 페이지 갯수

		int cnt = 0; // 예금상품 수
		int start = 0; // 현재 페이지 시작 글 번호
		int end = 0; // 현재 페이지 마지막 글 번호
		int number = 0; // 출력용 글 번호
		String pageNum = ""; // 페이지 번호
		int currentPage = 0; // 현재 페이지

		int pageCount = 0; // 페이지 갯수
		int startPage = 0; // 시작 페이지
		int endPage = 0; // 마지막 페이지

		// 검색 된 예금 상품 수 조회
		cnt = dao.getDepositProductSearchCnt(search);
		System.out.println("검색 된 예금 상품 수 : " + cnt);

		pageNum = req.getParameter("pageNum");

		if (pageNum == null) {
			pageNum = "1"; // 첫 페이지를 1페이지로 지정
		}

		// 상품 30건 기준
		currentPage = Integer.parseInt(pageNum);
		System.out.println("currentPage : " + currentPage);

		// 페이지 갯수 6 = (회원수 30건 / 한 페이지당 10개) + 나머지0
		pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지가 있으면 1페이지 추가

		// 현재 페이지 시작 글 번호(페이지별)
		// start = (currentPage - 1) * pageSize + 1;
		// 1 = (1 - 1) * 10 + 1
		start = (currentPage - 1) * pageSize + 1;

		// 현재 페이지 시작 글 번호(페이지별)
		// end = start + pageSize - 1;
		// 10 = 1 + 10 - 1
		end = start + pageSize - 1;

		System.out.println("start : " + start);
		System.out.println("end : " + end);

		// 출력용 글 번호
		// number = cnt - (currentPage - 1) * pageSize;
		number = cnt - (currentPage - 1) * pageSize;

		System.out.println("number : " + number);
		System.out.println("pageSize : " + pageSize);

		// 시작 페이지
		// 1 = (1 / 3) * 3 + 1;
		// startPage = (currentPage / pageBlock) * pageBlock + 1;
		startPage = (currentPage / pageBlock) * pageBlock + 1;
		if (currentPage % pageBlock == 0) {
			startPage -= pageBlock;
		}
		System.out.println("startPage : " + startPage);

		// 마지막 페이지
		// 3 = 1 + 3 - 1
		endPage = startPage + pageBlock - 1;
		if (endPage > pageCount) {
			endPage = pageCount;
		}
		System.out.println("endPage : " + endPage);

		System.out.println("===================================");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("start", start);
		map.put("end", end);
		map.put("search", search);

		ArrayList<DepositProductVO> dtos = null;
		if (cnt > 0) {
			// 5-2단계.
			dtos = dao.searchDepositProduct(map);
		}

		// 6단계. jsp로 전달하기 위해 request나 session에 처리결과를 저장
		model.addAttribute("dtos", dtos); // 검색된 예금 상품 목록
		model.addAttribute("cnt", cnt); // 예금 상품 수
		model.addAttribute("pageNum", pageNum); // 페이지 번호
		model.addAttribute("number", number); // 출력용 번호
		model.addAttribute("search", search); // 검색어
		if (cnt > 0) {
			model.addAttribute("startPage", startPage); // 시작 페이지
			model.addAttribute("endPage", endPage); // 마지막 페이지
			model.addAttribute("pageBlock", pageBlock); // 한 블럭당 페이지 갯수
			model.addAttribute("pageCount", pageCount); // 페이지 갯수
			model.addAttribute("currentPage", currentPage); // 현재 페이지
		}
	}

	// 예금 상품 상세보기
	@Override
	public void depositDetail(HttpServletRequest req, Model model) {
		// http://localhost/jsp_mvcBoard_jjh/boardDetail.bo?=num=30&pageNum=1&number=30
		String deposit_product_name = req.getParameter("deposit_product_name");
		System.out.println("deposit_product_name : " + deposit_product_name);
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int number = Integer.parseInt(req.getParameter("number"));

		// 5-2단계. 게시글 상세페이지 조회
		// getQnaDetail
		DepositProductVO vo = dao.getDepositDetail(deposit_product_name);

		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("dto", vo);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("number", number);
	}

	// 예금 신청 하기상세 화면
	@Override
	public void setDepositProductJoin(HttpServletRequest req, Model model) {
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int number = Integer.parseInt(req.getParameter("number"));
		String deposit_product_name = req.getParameter("deposit_product_name");
		String deposit_product_interRate = req.getParameter("deposit_product_interRate");
		Float interRate = Float.valueOf(deposit_product_interRate);
		String deposit_product_summary = req.getParameter("deposit_product_summary");
		String id = req.getParameter("customerID");

		String unique_key = dao.getUniqueKey(id);
		String account_id = createAccountId(Integer.parseInt(req.getParameter("deposit_product_bankCode")));
		// 작은 바구니 생성
		DepositProductVO vo = new DepositProductVO();
		vo.setDeposit_product_name(deposit_product_name);
		vo.setDeposit_product_notice(req.getParameter("deposit_product_notice"));
		vo.setDeposit_product_bankCode(Integer.parseInt(req.getParameter("deposit_product_bankCode")));
		vo.setDeposit_product_interRate(interRate);
		vo.setDeposit_product_minPrice(Integer.parseInt(req.getParameter("deposit_product_minPrice")));
		vo.setDeposit_product_summary(deposit_product_summary);

		req.setAttribute("unique_key", unique_key);
		req.setAttribute("account_id", account_id);
		req.setAttribute("dto", vo);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("number", number);

	}

	// 예금 가입시 계좌 개설(insert account)
	@Override
	public void makeDepositAccount(HttpServletRequest req, Model model) {
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int number = Integer.parseInt(req.getParameter("number"));

		String enPassword = bCryptPasswordEncoder.encode(req.getParameter("account_password"));

		AccountVO vo = new AccountVO();
		vo.setAccount_id(req.getParameter("account_id"));
		vo.setMember_id((String) req.getSession().getAttribute("customerID"));
		vo.setAccount_password(enPassword);
		vo.setAccount_type(1); // account_type => 예금 (1)
		// vo.setAccount_limit(account_limit);
		vo.setAccount_bankCode(Integer.parseInt(req.getParameter("account_bankCode")));
		vo.setUnique_key(req.getParameter("unique_key"));
		vo.setAccount_balance(Integer.parseInt(req.getParameter("account_balance")));
		// 예금은 한도 = 예치금 = 잔액

		int insertCnt = dao.insertAccount(vo);

		req.setAttribute("insertCnt", insertCnt);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("number", number);
	}

	// 예금 가입시 예금(deposit) 테이블 insert(지현)
	@Override
	public void insertDeposit(HttpServletRequest req, Model model) {
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int number = Integer.parseInt(req.getParameter("number"));
		String deposit_product_name = req.getParameter("deposit_product_name");

		// 작은바구니 생성
		DepositVO vo = new DepositVO();

		String account_id = req.getParameter("account_id");
		vo.setDeposit_product_name(deposit_product_name);
		vo.setAccount_id(account_id);
		String deposit_rate = req.getParameter("deposit_product_interRate");
		Float rate = Float.valueOf(deposit_rate);
		vo.setDeposit_rate(rate);
		vo.setDeposit_type(Integer.parseInt(req.getParameter("deposit_product_type")));

		// 화면에서 입력받은 기간 계산해 endDate(만기일) 설정함
		Long deposit_term = Long.parseLong(req.getParameter("deposit_term"));
		LocalDate now = LocalDate.now();
		now = now.plusMonths(deposit_term);
		Date date = Date.valueOf(now.toString());

		vo.setDeposit_endDate(date);

		vo.setDeposit_balance(Integer.parseInt(req.getParameter("account_balance")));

		int insertDeposit = dao.insertDeposit(vo);

		req.setAttribute("insertDeposit", insertDeposit);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("number", number);

	}

	// 연금 상품 조회 (지현)
	@Override
	public void irpList(HttpServletRequest req, Model model) {
		// 3단계. 화면으로부터 입력받은 값을 받아온다.
		// 페이징
		int pageSize = 8; // 한페이지당 출력할 글 갯수
		int pageBlock = 3; // 한 블록당 페이지 갯수

		int cnt = 0; // 글 갯수
		int start = 0; // 현재페이지 시작 글 번호
		int end = 0; // 현재페이지 마지막 글 번호
		int number = 0; // 출력용 글번호
		String pageNum = ""; // 페이지 번호
		int currentPage = 0; // 현재 페이지

		int pageCount = 0; // 페이지 갯수
		int startPage = 0; // 시작페이지
		int endPage = 0; // 마지막페이지

		// 5-1단계. 게시글 갯수 조회
		cnt = dao.getIrpCnt();

		System.out.println("cnt ==> " + cnt);

		// 5-2단계. 게시글 목록 조회
		pageNum = req.getParameter("pageNum");

		if (pageNum == null) {
			pageNum = "1"; // 첫페이지를 1페이지로 지정
		}

		// 글 30건 기준
		currentPage = Integer.parseInt(pageNum);
		System.out.println("currentPage : " + currentPage);

		// 페이지 갯수 6 = (30/5) + (0)
		pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지 있으면 1페이지

		// 현재페이지 시작 글번호(페이지별)
		// start = (currentPage - 1) * pageSize +1;
		// 1 = (1 - 1 )* 5 + 1
		start = (currentPage - 1) * pageSize + 1;

		// 현재페이지 마지막 글번호(페이지별)
		// end = start + pageSize - 1;
		// 5 = 1 + 5 - 1
		end = start + pageSize - 1;

		System.out.println("start : " + start);
		System.out.println("end : " + end);

		// 출력용 글번호
		// 30 = 30 - (1 - 1) * 5 //1페이지
		// number = cnt- (currentPage - 1) * pageSize;
		number = cnt - (currentPage - 1) * pageSize;

		System.out.println("number : " + number);
		System.out.println("pageSize : " + pageSize);

		// 시작페이지
		// 1 = (1 / 3) * 3 + 1;
		// startPage = (currentPage / pageBlock) * pageBlock + 1;
		startPage = (currentPage / pageBlock) * pageBlock + 1;
		if (currentPage % pageBlock == 0)
			startPage -= pageBlock;

		System.out.println("startPage : " + startPage);

		// 마지막 페이지
		// 3 = 1 + 3 - 1
		endPage = startPage + pageBlock - 1;
		if (endPage > pageCount)
			endPage = pageCount;

		System.out.println("endPage : " + endPage);

		System.out.println("--------------------------");

		List<IrpProductVO> dtos = null;

		if (cnt > 0) {
			// 5-2단계. 게시글 목록 조회
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("start", start);
			map.put("end", end);
			dtos = dao.getIrpList(map);
		}

		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("dtos", dtos); // 게시글 목록
		req.setAttribute("cnt", cnt); // 글개수
		req.setAttribute("pageNum", pageNum); // 페이지 번호
		req.setAttribute("number", number); // 출력용 글번호

		if (cnt > 0) {
			req.setAttribute("startPage", startPage); // 시작페이지
			req.setAttribute("endPage", endPage); // 마지막 페이지
			req.setAttribute("pageBlock", pageBlock); // 한블럭당 페이지 갯수
			req.setAttribute("pageCount", pageCount); // 페이지 갯수
			req.setAttribute("currentPage", currentPage); // 현재페이지
		}
	}

	// 연금 상품 검색
	@Override
	public void irpProductSearch(HttpServletRequest req, Model model) {

		// 입력받은 검색어
		String search = req.getParameter("search");
		System.out.println("관리자 페이지 회원 검색어 : " + search);

		// 페이징
		int pageSize = 10; // 한 페이지당 출력할 예금상품
		int pageBlock = 3; // 한 블럭당 페이지 갯수

		int cnt = 0; // 예금상품 수
		int start = 0; // 현재 페이지 시작 글 번호
		int end = 0; // 현재 페이지 마지막 글 번호
		int number = 0; // 출력용 글 번호
		String pageNum = ""; // 페이지 번호
		int currentPage = 0; // 현재 페이지

		int pageCount = 0; // 페이지 갯수
		int startPage = 0; // 시작 페이지
		int endPage = 0; // 마지막 페이지

		// 검색 된 예금 상품 수 조회
		cnt = dao.getIrpProductSearchCnt(search);
		System.out.println("검색 된 적금 상품 수 : " + cnt);

		pageNum = req.getParameter("pageNum");

		if (pageNum == null) {
			pageNum = "1"; // 첫 페이지를 1페이지로 지정
		}

		// 상품 30건 기준
		currentPage = Integer.parseInt(pageNum);
		System.out.println("currentPage : " + currentPage);

		// 페이지 갯수 6 = (회원수 30건 / 한 페이지당 10개) + 나머지0
		pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지가 있으면 1페이지 추가

		// 현재 페이지 시작 글 번호(페이지별)
		// start = (currentPage - 1) * pageSize + 1;
		// 1 = (1 - 1) * 10 + 1
		start = (currentPage - 1) * pageSize + 1;

		// 현재 페이지 시작 글 번호(페이지별)
		// end = start + pageSize - 1;
		// 10 = 1 + 10 - 1
		end = start + pageSize - 1;

		System.out.println("start : " + start);
		System.out.println("end : " + end);

		// 출력용 글 번호
		// number = cnt - (currentPage - 1) * pageSize;
		number = cnt - (currentPage - 1) * pageSize;

		System.out.println("number : " + number);
		System.out.println("pageSize : " + pageSize);

		// 시작 페이지
		// 1 = (1 / 3) * 3 + 1;
		// startPage = (currentPage / pageBlock) * pageBlock + 1;
		startPage = (currentPage / pageBlock) * pageBlock + 1;
		if (currentPage % pageBlock == 0) {
			startPage -= pageBlock;
		}
		System.out.println("startPage : " + startPage);

		// 마지막 페이지
		// 3 = 1 + 3 - 1
		endPage = startPage + pageBlock - 1;
		if (endPage > pageCount) {
			endPage = pageCount;
		}
		System.out.println("endPage : " + endPage);

		System.out.println("===================================");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("start", start);
		map.put("end", end);
		map.put("search", search);

		ArrayList<IrpProductVO> dtos = null;
		if (cnt > 0) {
			// 5-2단계. 회원수 조회
			dtos = dao.searchIrpProduct(map);
		}

		// 6단계. jsp로 전달하기 위해 request나 session에 처리결과를 저장
		model.addAttribute("dtos", dtos); // 검색된 적금 상품 목록
		model.addAttribute("cnt", cnt); // 적금 상품 수
		model.addAttribute("pageNum", pageNum); // 페이지 번호
		model.addAttribute("number", number); // 출력용 번호
		model.addAttribute("search", search); // 검색어
		if (cnt > 0) {
			model.addAttribute("startPage", startPage); // 시작 페이지
			model.addAttribute("endPage", endPage); // 마지막 페이지
			model.addAttribute("pageBlock", pageBlock); // 한 블럭당 페이지 갯수
			model.addAttribute("pageCount", pageCount); // 페이지 갯수
			model.addAttribute("currentPage", currentPage); // 현재 페이지
		}
	}

	// 연금 상품 상세보기
	@Override
	public void irpDetail(HttpServletRequest req, Model model) {
		String irp_product_name = req.getParameter("irp_product_name");
		System.out.println("irp_product_name : " + irp_product_name);
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int number = Integer.parseInt(req.getParameter("number"));

		// 5-2단계. 게시글 상세페이지 조회
		// getQnaDetail
		IrpProductVO vo = dao.getIrpDetail(irp_product_name);

		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("dto", vo);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("number", number);
	}

	// 연금 신청 상세화면
	@Override
	public void irpProductJoin(HttpServletRequest req, Model model) {
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int number = Integer.parseInt(req.getParameter("number"));
		String id = (String) req.getSession().getAttribute("customerID");

		String unique_key = dao.getUniqueKey(id);
		String account_id = createAccountId(Integer.parseInt(req.getParameter("irp_product_bankCode")));
		// 작은 바구니 생성
		IrpProductVO vo = new IrpProductVO();
		vo.setIrp_product_name(req.getParameter("irp_product_name"));
		vo.setIrp_product_bankCode(Integer.parseInt(req.getParameter("irp_product_bankCode")));
		vo.setIrp_product_interRate(Float.valueOf(req.getParameter("irp_product_interRate")));
		vo.setIrp_product_summary(req.getParameter("irp_product_summary"));
		vo.setIrp_product_money(Integer.parseInt(req.getParameter("irp_product_money")));
		vo.setIrp_product_expiryTerm(Integer.parseInt(req.getParameter("irp_product_expiryTerm")));
		vo.setIrp_product_notice(req.getParameter("irp_product_notice"));

		System.out.println("연금 계좌번호 =>" + account_id);

		req.setAttribute("unique_key", unique_key);
		req.setAttribute("account_id", account_id);
		req.setAttribute("dto", vo);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("number", number);
	}

	// 연금용 계좌 개설
	@Override
	public void makeIrpAccount(HttpServletRequest req, Model model) {
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int number = Integer.parseInt(req.getParameter("number"));

		String enPassword = bCryptPasswordEncoder.encode(req.getParameter("account_password"));

		AccountVO vo = new AccountVO();
		vo.setAccount_id(req.getParameter("account_id"));
		vo.setMember_id((String) req.getSession().getAttribute("customerID"));
		vo.setAccount_password(enPassword);
		vo.setAccount_type(4); // account_type => 연금 (4)
		// vo.setAccount_limit(account_limit);
		vo.setAccount_bankCode(Integer.parseInt(req.getParameter("account_bankCode")));
		vo.setUnique_key(req.getParameter("unique_key"));
		vo.setAccount_balance(Integer.parseInt(req.getParameter("irp_product_money")));

		int insertCnt = dao.insertAccount(vo);

		req.setAttribute("insertCnt", insertCnt);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("number", number);
	}

	// 연금 가입시 연금(irp) 테이블 insert
	@Override
	public void insertIrp(HttpServletRequest req, Model model) {
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int number = Integer.parseInt(req.getParameter("number"));
		String irp_product_name = req.getParameter("product_name");
		String member_id = (String) req.getSession().getAttribute("customerID");
		String account_id = req.getParameter("account_id");
		String deposit_rate = req.getParameter("product_interRate");
		Float rate = Float.valueOf(deposit_rate);

		// 작은바구니 생성
		IrpVO vo = new IrpVO();
		vo.setIrp_product_name(irp_product_name);
		vo.setAccount_id(account_id);
		vo.setMember_id(member_id);
		vo.setIrp_rate(rate);
		vo.setIrp_money(Integer.parseInt(req.getParameter("irp_product_money")));
		// 화면에서 입력받은 기간 계산해 endDate(만기일) 설정함
		Long irp_product_expiryTerm = Long.parseLong(req.getParameter("irp_product_expiryTerm"));
		LocalDate now = LocalDate.now();
		now = now.plusMonths(irp_product_expiryTerm);
		Date date = Date.valueOf(now.toString());
		vo.setIrp_endDate(date);

		int insertIrp = dao.insertIrp(vo);

		req.setAttribute("insertIrp", insertIrp);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("number", number);

	}

	// 적금 상품 조회
	@Override
	public void savingList(HttpServletRequest req, Model model) {
		// 3단계. 화면으로부터 입력받은 값을 받아온다.
		// 페이징
		int pageSize = 8; // 한페이지당 출력할 글 갯수
		int pageBlock = 3; // 한 블록당 페이지 갯수

		int cnt = 0; // 글 갯수
		int start = 0; // 현재페이지 시작 글 번호
		int end = 0; // 현재페이지 마지막 글 번호
		int number = 0; // 출력용 글번호
		String pageNum = ""; // 페이지 번호
		int currentPage = 0; // 현재 페이지

		int pageCount = 0; // 페이지 갯수
		int startPage = 0; // 시작페이지
		int endPage = 0; // 마지막페이지

		// 5-1단계. 게시글 갯수 조회
		cnt = dao.getSavingCnt();

		System.out.println("cnt ==> " + cnt);

		// 5-2단계. 게시글 목록 조회
		pageNum = req.getParameter("pageNum");

		if (pageNum == null) {
			pageNum = "1"; // 첫페이지를 1페이지로 지정
		}

		// 글 30건 기준
		currentPage = Integer.parseInt(pageNum);
		System.out.println("currentPage : " + currentPage);

		// 페이지 갯수 6 = (30/5) + (0)
		pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지 있으면 1페이지

		// 현재페이지 시작 글번호(페이지별)
		// start = (currentPage - 1) * pageSize +1;
		// 1 = (1 - 1 )* 5 + 1
		start = (currentPage - 1) * pageSize + 1;

		// 현재페이지 마지막 글번호(페이지별)
		// end = start + pageSize - 1;
		// 5 = 1 + 5 - 1
		end = start + pageSize - 1;

		System.out.println("start : " + start);
		System.out.println("end : " + end);

		// 출력용 글번호
		// 30 = 30 - (1 - 1) * 5 //1페이지
		// number = cnt- (currentPage - 1) * pageSize;
		number = cnt - (currentPage - 1) * pageSize;

		System.out.println("number : " + number);
		System.out.println("pageSize : " + pageSize);

		// 시작페이지
		// 1 = (1 / 3) * 3 + 1;
		// startPage = (currentPage / pageBlock) * pageBlock + 1;
		startPage = (currentPage / pageBlock) * pageBlock + 1;
		if (currentPage % pageBlock == 0)
			startPage -= pageBlock;

		System.out.println("startPage : " + startPage);

		// 마지막 페이지
		// 3 = 1 + 3 - 1
		endPage = startPage + pageBlock - 1;
		if (endPage > pageCount)
			endPage = pageCount;

		System.out.println("endPage : " + endPage);

		System.out.println("--------------------------");

		List<SavingProductVO> dtos = null;

		if (cnt > 0) {
			// 5-2단계. 게시글 목록 조회
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("start", start);
			map.put("end", end);
			dtos = dao.getSavingList(map);
		}

		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("dtos", dtos); // 게시글 목록
		req.setAttribute("cnt", cnt); // 글개수
		req.setAttribute("pageNum", pageNum); // 페이지 번호
		req.setAttribute("number", number); // 출력용 글번호

		if (cnt > 0) {
			req.setAttribute("startPage", startPage); // 시작페이지
			req.setAttribute("endPage", endPage); // 마지막 페이지
			req.setAttribute("pageBlock", pageBlock); // 한블럭당 페이지 갯수
			req.setAttribute("pageCount", pageCount); // 페이지 갯수
			req.setAttribute("currentPage", currentPage); // 현재페이지
		}
	}

	// 적금 상품 검색
	@Override
	public void savingProductSearch(HttpServletRequest req, Model model) {

		// 입력받은 검색어
		String search = req.getParameter("search");
		System.out.println("관리자 페이지 회원 검색어 : " + search);

		// 페이징
		int pageSize = 10; // 한 페이지당 출력할 적금상품
		int pageBlock = 3; // 한 블럭당 페이지 갯수

		int cnt = 0; // 적금상품 수
		int start = 0; // 현재 페이지 시작 글 번호
		int end = 0; // 현재 페이지 마지막 글 번호
		int number = 0; // 출력용 글 번호
		String pageNum = ""; // 페이지 번호
		int currentPage = 0; // 현재 페이지

		int pageCount = 0; // 페이지 갯수
		int startPage = 0; // 시작 페이지
		int endPage = 0; // 마지막 페이지

		// 검색 된 예금 상품 수 조회
		cnt = dao.getSavingProductSearchCnt(search);
		System.out.println("검색 된 적금 상품 수 : " + cnt);

		pageNum = req.getParameter("pageNum");

		if (pageNum == null) {
			pageNum = "1"; // 첫 페이지를 1페이지로 지정
		}

		// 상품 30건 기준
		currentPage = Integer.parseInt(pageNum);
		System.out.println("currentPage : " + currentPage);

		// 페이지 갯수 6 = (회원수 30건 / 한 페이지당 10개) + 나머지0
		pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지가 있으면 1페이지 추가

		// 현재 페이지 시작 글 번호(페이지별)
		// start = (currentPage - 1) * pageSize + 1;
		// 1 = (1 - 1) * 10 + 1
		start = (currentPage - 1) * pageSize + 1;

		// 현재 페이지 시작 글 번호(페이지별)
		// end = start + pageSize - 1;
		// 10 = 1 + 10 - 1
		end = start + pageSize - 1;

		System.out.println("start : " + start);
		System.out.println("end : " + end);

		// 출력용 글 번호
		// number = cnt - (currentPage - 1) * pageSize;
		number = cnt - (currentPage - 1) * pageSize;

		System.out.println("number : " + number);
		System.out.println("pageSize : " + pageSize);

		// 시작 페이지
		// 1 = (1 / 3) * 3 + 1;
		// startPage = (currentPage / pageBlock) * pageBlock + 1;
		startPage = (currentPage / pageBlock) * pageBlock + 1;
		if (currentPage % pageBlock == 0) {
			startPage -= pageBlock;
		}
		System.out.println("startPage : " + startPage);

		// 마지막 페이지
		// 3 = 1 + 3 - 1
		endPage = startPage + pageBlock - 1;
		if (endPage > pageCount) {
			endPage = pageCount;
		}
		System.out.println("endPage : " + endPage);

		System.out.println("===================================");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("start", start);
		map.put("end", end);
		map.put("search", search);

		ArrayList<SavingProductVO> dtos = null;
		if (cnt > 0) {
			// 5-2단계. 회원수 조회
			dtos = dao.searchSavingProduct(map);
		}

		// 6단계. jsp로 전달하기 위해 request나 session에 처리결과를 저장
		model.addAttribute("dtos", dtos); // 검색된 적금 상품 목록
		model.addAttribute("cnt", cnt); // 적금 상품 수
		model.addAttribute("pageNum", pageNum); // 페이지 번호
		model.addAttribute("number", number); // 출력용 번호
		model.addAttribute("search", search); // 검색어
		if (cnt > 0) {
			model.addAttribute("startPage", startPage); // 시작 페이지
			model.addAttribute("endPage", endPage); // 마지막 페이지
			model.addAttribute("pageBlock", pageBlock); // 한 블럭당 페이지 갯수
			model.addAttribute("pageCount", pageCount); // 페이지 갯수
			model.addAttribute("currentPage", currentPage); // 현재 페이지
		}
	}

	// 적금 상품 상세보기
	@Override
	public void savingDetail(HttpServletRequest req, Model model) {
		String saving_product_name = req.getParameter("saving_product_name");
		System.out.println("saving_product_name : " + saving_product_name);
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int number = Integer.parseInt(req.getParameter("number"));

		// 5-2단계. 게시글 상세페이지 조회
		// getQnaDetail
		SavingProductVO vo = dao.getSavingDetail(saving_product_name);

		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("dto", vo);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("number", number);
	}

	// 적금 신청
	@Override
	public void savingProductAction(HttpServletRequest req, Model model) {

		// 작은바구니 생성

		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int number = Integer.parseInt(req.getParameter("number"));
		String id = (String) req.getSession().getAttribute("customerID");

		// members 테이블에 있는 unique키 가져오기
		String unique_key = dao.getUniqueKey(id);

		// 계좌생성 메서드 -> 은행코드 가져와서 생성
		String account_id = createAccountId(Integer.parseInt(req.getParameter("deposit_product_bankCode")));

		SavingProductVO vo = new SavingProductVO();
		vo.setSaving_product_name(req.getParameter("saving_product_name"));
		vo.setSaving_product_interRate(Float.parseFloat(req.getParameter("saving_product_interRate")));
		vo.setSaving_product_type(Integer.parseInt(req.getParameter("saving_product_type")));
		vo.setSaving_product_maxDate(Integer.parseInt(req.getParameter("saving_product_maxDate")));
		vo.setSaving_product_minDate(Integer.parseInt(req.getParameter("saving_product_minDate")));
		vo.setSaving_product_minPrice(Integer.parseInt(req.getParameter("saving_product_minPrice")));
		vo.setSaving_product_bankCode(Integer.parseInt(req.getParameter("saving_product_bankCode")));
		vo.setSaving_product_summary(req.getParameter("saving_product_summary"));

		int insertCnt = dao.savingProductAction(vo);

		model.addAttribute("unique_key", unique_key);
		model.addAttribute("account_id", account_id);
		model.addAttribute("vo", vo);
		model.addAttribute("pageNum", pageNum);
		model.addAttribute("number", number);
		model.addAttribute("inserCnt", insertCnt);
	}

	//
	// 적금 상품 조회
	@Override
	public void fundList(HttpServletRequest req, Model model) {
		// 3단계. 화면으로부터 입력받은 값을 받아온다.
		// 페이징
		int pageSize = 8; // 한페이지당 출력할 글 갯수
		int pageBlock = 3; // 한 블록당 페이지 갯수

		int cnt = 0; // 글 갯수
		int start = 0; // 현재페이지 시작 글 번호
		int end = 0; // 현재페이지 마지막 글 번호
		int number = 0; // 출력용 글번호
		String pageNum = ""; // 페이지 번호
		int currentPage = 0; // 현재 페이지

		int pageCount = 0; // 페이지 갯수
		int startPage = 0; // 시작페이지
		int endPage = 0; // 마지막페이지

		// 5-1단계. 게시글 갯수 조회
		cnt = dao.getFundCnt();

		System.out.println("cnt ==> " + cnt);

		// 5-2단계. 게시글 목록 조회
		pageNum = req.getParameter("pageNum");

		if (pageNum == null) {
			pageNum = "1"; // 첫페이지를 1페이지로 지정
		}

		// 글 30건 기준
		currentPage = Integer.parseInt(pageNum);
		System.out.println("currentPage : " + currentPage);

		// 페이지 갯수 6 = (30/5) + (0)
		pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지 있으면 1페이지

		// 현재페이지 시작 글번호(페이지별)
		// start = (currentPage - 1) * pageSize +1;
		// 1 = (1 - 1 )* 5 + 1
		start = (currentPage - 1) * pageSize + 1;

		// 현재페이지 마지막 글번호(페이지별)
		// end = start + pageSize - 1;
		// 5 = 1 + 5 - 1
		end = start + pageSize - 1;

		System.out.println("start : " + start);
		System.out.println("end : " + end);

		// 출력용 글번호
		// 30 = 30 - (1 - 1) * 5 //1페이지
		// number = cnt- (currentPage - 1) * pageSize;
		number = cnt - (currentPage - 1) * pageSize;

		System.out.println("number : " + number);
		System.out.println("pageSize : " + pageSize);

		// 시작페이지
		// 1 = (1 / 3) * 3 + 1;
		// startPage = (currentPage / pageBlock) * pageBlock + 1;
		startPage = (currentPage / pageBlock) * pageBlock + 1;
		if (currentPage % pageBlock == 0)
			startPage -= pageBlock;

		System.out.println("startPage : " + startPage);

		// 마지막 페이지
		// 3 = 1 + 3 - 1
		endPage = startPage + pageBlock - 1;
		if (endPage > pageCount)
			endPage = pageCount;

		System.out.println("endPage : " + endPage);

		System.out.println("--------------------------");

		List<FundProductVO> dtos = null;

		if (cnt > 0) {
			// 5-2단계. 게시글 목록 조회
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("start", start);
			map.put("end", end);
			dtos = dao.getFundList(map);
		}

		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("dtos", dtos); // 게시글 목록
		req.setAttribute("cnt", cnt); // 글개수
		req.setAttribute("pageNum", pageNum); // 페이지 번호
		req.setAttribute("number", number); // 출력용 글번호

		if (cnt > 0) {
			req.setAttribute("startPage", startPage); // 시작페이지
			req.setAttribute("endPage", endPage); // 마지막 페이지
			req.setAttribute("pageBlock", pageBlock); // 한블럭당 페이지 갯수
			req.setAttribute("pageCount", pageCount); // 페이지 갯수
			req.setAttribute("currentPage", currentPage); // 현재페이지
		}
	}

	// 펀드 상품 검색
	@Override
	public void fundProductSearch(HttpServletRequest req, Model model) {

		// 입력받은 검색어
		String search = req.getParameter("search");
		System.out.println("관리자 페이지 회원 검색어 : " + search);

		// 페이징
		int pageSize = 10; // 한 페이지당 출력할 펀드상품
		int pageBlock = 3; // 한 블럭당 페이지 갯수

		int cnt = 0; // 펀드상품 수
		int start = 0; // 현재 페이지 시작 글 번호
		int end = 0; // 현재 페이지 마지막 글 번호
		int number = 0; // 출력용 글 번호
		String pageNum = ""; // 페이지 번호
		int currentPage = 0; // 현재 페이지

		int pageCount = 0; // 페이지 갯수
		int startPage = 0; // 시작 페이지
		int endPage = 0; // 마지막 페이지

		// 검색 된 펀드 상품 수 조회
		cnt = dao.getFundProductSearchCnt(search);
		System.out.println("검색 된 펀드 상품 수 : " + cnt);

		pageNum = req.getParameter("pageNum");

		if (pageNum == null) {
			pageNum = "1"; // 첫 페이지를 1페이지로 지정
		}

		// 상품 30건 기준
		currentPage = Integer.parseInt(pageNum);
		System.out.println("currentPage : " + currentPage);

		// 페이지 갯수 6 = (회원수 30건 / 한 페이지당 10개) + 나머지0
		pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지가 있으면 1페이지 추가

		// 현재 페이지 시작 글 번호(페이지별)
		// start = (currentPage - 1) * pageSize + 1;
		// 1 = (1 - 1) * 10 + 1
		start = (currentPage - 1) * pageSize + 1;

		// 현재 페이지 시작 글 번호(페이지별)
		// end = start + pageSize - 1;
		// 10 = 1 + 10 - 1
		end = start + pageSize - 1;

		System.out.println("start : " + start);
		System.out.println("end : " + end);

		// 출력용 글 번호
		// number = cnt - (currentPage - 1) * pageSize;
		number = cnt - (currentPage - 1) * pageSize;

		System.out.println("number : " + number);
		System.out.println("pageSize : " + pageSize);

		// 시작 페이지
		// 1 = (1 / 3) * 3 + 1;
		// startPage = (currentPage / pageBlock) * pageBlock + 1;
		startPage = (currentPage / pageBlock) * pageBlock + 1;
		if (currentPage % pageBlock == 0) {
			startPage -= pageBlock;
		}
		System.out.println("startPage : " + startPage);

		// 마지막 페이지
		// 3 = 1 + 3 - 1
		endPage = startPage + pageBlock - 1;
		if (endPage > pageCount) {
			endPage = pageCount;
		}
		System.out.println("endPage : " + endPage);

		System.out.println("===================================");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("start", start);
		map.put("end", end);
		map.put("search", search);

		ArrayList<FundProductVO> dtos = null;
		if (cnt > 0) {
			// 5-2단계. 회원수 조회
			dtos = dao.searchFundProduct(map);
		}

		// 6단계. jsp로 전달하기 위해 request나 session에 처리결과를 저장
		model.addAttribute("dtos", dtos); // 검색된 펀드 상품 목록
		model.addAttribute("cnt", cnt); // 적금 상품 수
		model.addAttribute("pageNum", pageNum); // 페이지 번호
		model.addAttribute("number", number); // 출력용 번호
		model.addAttribute("search", search); // 검색어
		if (cnt > 0) {
			model.addAttribute("startPage", startPage); // 시작 페이지
			model.addAttribute("endPage", endPage); // 마지막 페이지
			model.addAttribute("pageBlock", pageBlock); // 한 블럭당 페이지 갯수
			model.addAttribute("pageCount", pageCount); // 페이지 갯수
			model.addAttribute("currentPage", currentPage); // 현재 페이지
		}
	}

	// 펀드 상품 상세보기
	@Override
	public void fundDetail(HttpServletRequest req, Model model) {
		String fund_title = req.getParameter("fund_title");
		System.out.println("fund_title : " + fund_title);
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int number = Integer.parseInt(req.getParameter("number"));

		// 5-2단계. 게시글 상세페이지 조회
		// getQnaDetail
		FundProductVO vo = dao.getFundDetail(fund_title);

		// 6단계. jsp로 전달하기 위해 request나 session에 처리 결과를 저장
		req.setAttribute("dto", vo);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("number", number);
	}

	// 펀드 신청
	@Override
	public void fundProductAction(HttpServletRequest req, Model model) {

		// 작은바구니 생성
		FundProductVO vo = new FundProductVO();
//	        vo.setSaving_product_name(req.getParameter("saving_product_name"));
//	        vo.setSaving_product_interRate(Float.parseFloat(req.getParameter("saving_product_interRate")));
//	        vo.setSaving_product_type(Integer.parseInt(req.getParameter("saving_product_type")));
//	        vo.setSaving_product_maxDate(Integer.parseInt(req.getParameter("saving_product_maxDate")));
//	        vo.setSaving_product_minDate(Integer.parseInt(req.getParameter("saving_product_minDate")));
//	        vo.setSaving_product_minPrice(Integer.parseInt(req.getParameter("saving_product_minPrice")));
//	        vo.setSaving_product_bankCode(Integer.parseInt(req.getParameter("saving_product_bankCode")));

		int insertCnt = dao.fundProductAction(vo);

		model.addAttribute("inserCnt", insertCnt);
	}

	// 환율 데이터 입력 후 출력(지호)
	// @Scheduled(cron = "0 0/5 9-17 * * *") // 9시부터 17시까지
	// @Scheduled(fixedRate = 6000) // 1분마다 한번씩
	@Override
	public void exchanges(Model model) {

		String country = "";
		String strJson = "";
		String rate = "";
		String exchange_country = "";
		JSONArray array = null;
		JSONObject obj = null;
		JSONObject obj2 = null;
		int num = 0;
		List<CrawlerVO> list = null;
		CrawlerVO vo = null;
		// db에 있는 환율 가져올 list
		// List<String> listRate =null;
		String listRate = "";
		try {
			strJson = Jsoup.connect("http://fx.kebhana.com/FER1101M.web").get().select("body").text()
					.replaceAll("},] }", "} ]");
			// 0번째 부터 시작
			strJson = strJson.substring(strJson.indexOf("["));
			list = new ArrayList<CrawlerVO>();

			// JSONArray에 "리스트" : [] 출력
			array = new JSONArray(strJson);
			// System.out.println("array.length :" + array.length()); // 49
			// 환율 db체크
			obj2 = new JSONObject(array.get(1).toString());
			exchange_country = obj2.get("통화명").toString();
			num = dao.exchangeChk(exchange_country);
			System.out.println("num : " + num);

			// 환율 데이터 저장
			if (num != 1) {
				for (int i = 0; i < array.length(); i++) {
					System.out.println("환율 데이터 저장");
					obj = new JSONObject(array.get(i).toString());
					country = obj.get("통화명").toString();
					rate = obj.get("매매기준율").toString();
					vo = new CrawlerVO(country, rate);
					dao.exchangeIn(vo);

					// 화면 출력용
					if (i < 6) {
						obj = new JSONObject(array.get(i).toString());
						country = obj.get("통화명").toString();
						rate = obj.get("매매기준율").toString();
						vo = new CrawlerVO(country, rate);
						list.add(vo);
					}

				}
			} else {
				// 환율 최신화
				System.out.println("array.length :" + array.length());
				for (int i = 0; i < array.length(); i++) {
					System.out.println("환율 최신화");
					obj = new JSONObject(array.get(i).toString());
					country = obj.get("통화명").toString();
					rate = obj.get("매매기준율").toString();

					// 최신화 전 환율 비교
					listRate = dao.exchangeVary(country);

					// double lr = (((Double.parseDouble(rate)*100) /
					// Double.parseDouble(listRate.get(i))) -100)*100;
					double lr = (((Double.parseDouble(rate) * 100) / Double.parseDouble(listRate)) - 100) * 100;
					double compare = Math.round(lr * 100) / 100.0;
					System.out.println("compare : " + compare);

					String comStr = String.format("%1$,.2f", compare);
					// 최신화
					dao.exchangeUpd(vo);

					// 증감률 까지
					vo = new CrawlerVO(country, rate, comStr);
					list.add(vo);
					// 화면 출력용
//					if(i<6) {
//						obj = new JSONObject(array.get(i).toString());
//						country = obj.get("통화명").toString();
//						rate = obj.get("매매기준율").toString();
//						vo = new CrawlerVO(country, rate, compare);
//						list.add(vo);
//					}

				}
			}
		} catch (Exception e) {

		}
		model.addAttribute("list", list);
	}

	// 환율 목록 출력(지호)
	// @Scheduled(cron = "0 0/5 9-17 * * *") // 9시부터 17시까지
	// @Scheduled(fixedRate = 6000) // 1분마다 한번씩
	@Override
	public void exchangeList(HttpServletRequest req, Model model) {

		String strJson = "";
		String exchange_country = "";
		String exchange_rate = "";
		String exchange_buy = "";
		String exchange_sell = "";
		String exchange_transfer = "";
		String exchange_recive = "";
		JSONArray array = null;
		JSONObject obj = null;
		List<CrawlerVO> list = null;
		CrawlerVO vo = null;
		// db에 있는 환율 가져올 list
		try {
			strJson = Jsoup.connect("http://fx.kebhana.com/FER1101M.web").get().select("body").text()
					.replaceAll("},] }", "} ]");
			// 0번째 부터 시작
			strJson = strJson.substring(strJson.indexOf("["));
			list = new ArrayList<CrawlerVO>();

			// JSONArray에 "리스트" : [] 출력
			array = new JSONArray(strJson);

			// 환율 데이터 저장
			for (int i = 0; i < array.length(); i++) {
				System.out.println("환율 데이터 출력");
				obj = new JSONObject(array.get(i).toString());
				exchange_country = obj.get("통화명").toString();
				exchange_rate = obj.get("매매기준율").toString();
				exchange_buy = obj.get("현찰사실때").toString();
				exchange_sell = obj.get("현찰파실때").toString();
				exchange_transfer = obj.get("송금_전신환보내실때").toString();
				exchange_recive = obj.get("송금_전신환받으실때").toString();

				vo = new CrawlerVO(exchange_country, exchange_rate, exchange_buy, exchange_sell, exchange_transfer,
						exchange_recive);
				list.add(vo);

			}
		} catch (Exception e) {

		}
		model.addAttribute("list", list);
	}

	// 회원 계좌 찾기
	@Override
	public void getAccount(HttpServletRequest req, Model model) {

		String strId = (String) req.getSession().getAttribute("customerID");
		System.out.println("서비스확인(회원 아이디strId): " + strId);
		List<AccountVO> list = null;
		list = dao.accountFind(strId);

		System.out.println("list: " + list.size());

		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i) + " ");
		}

		model.addAttribute("dtos", list);
	}

	// 회원 계좌 비밀번호 확인
	@Override
	public void accountPwdConfirm(HttpServletRequest req, Model model) {
		String strId = req.getParameter("account_password");

		int cnt = dao.account_pwd(strId);
		System.out.println("서비스확인(account_password): " + cnt);

		model.addAttribute("selectCnt", cnt);
		model.addAttribute("account_password", strId);
	}

	// 회원 이체
	@Override
	public void transferConfirm(HttpServletRequest req, Model model) {

		String member_id = (String) req.getSession().getAttribute("CustomerID");
		String account_id = req.getParameter("account_id");
		int account_password = Integer.parseInt(req.getParameter("account_password"));

		int account_bank = Integer.parseInt(req.getParameter("account_bank"));
		String transfer_senderAccount = req.getParameter("transfer_senderAccount");
		int transfer_money = Integer.parseInt(req.getParameter("transfer_money"));

		System.out.println("서비스 확인(member_id): " + member_id);
		System.out.println("서비스 확인(account_id): " + account_id);
		System.out.println("서비스 확인(account_password): " + account_password);
		System.out.println("서비스 확인(account_bank): " + account_bank);

		TransferVO vo = new TransferVO();
		vo.setAccount_id(account_id);
		vo.setTransfer_bankCode(account_bank);
		vo.setTransfer_senderAccount(transfer_senderAccount);
		vo.setTransfer_money(transfer_money);

		String transfer_inComment = "";
		transfer_inComment = req.getParameter("transfer_inComment");
		if (!transfer_inComment.equals("")) {
			vo.setTransfer_inComment(transfer_inComment);
		}

		String transfer_outComment = "";
		transfer_outComment = req.getParameter("transfer_outComment");
		if (!transfer_outComment.equals("")) {
			vo.setTransfer_outComment(transfer_outComment);
		}

	}
	// test
//	@Override // json형태로 한번에 넣는
//	public void test(HttpServletRequest req, Model model) {
//		String country ="";
//		String strJson="";
//		String rate="";
//		String exchange_country="";
//		JSONArray array = null;
//		JSONObject obj = null;
//		JSONObject obj2 = null;
//		int num = 0;
//		List<String> list = null;
//		CrawlerVO vo = null;
//		// db에 있는 환율 가져올 list
//		//List<String> listRate =null;
//		String listRate = "";
//		try {
//			strJson = Jsoup.connect("http://fx.kebhana.com/FER1101M.web").get().select("body").text().replaceAll("},] }", "} ]");
//			strJson = strJson.substring(strJson.indexOf("["));
//			ObjectMapper mapper = new ObjectMapper();
//
//			String jsonStr = mapper.writeValueAsString(strJson);
//			String json1 = jsonStr.substring(0,3472);
//			String json2 = jsonStr.substring(3473, 7013);
//			list = new ArrayList<String>();
//			list.add(json1);
//			list.add(json2);
//			int insertCnt = dao.jsonIn(list);
//			System.out.println("insertCnt : " + insertCnt);
//		}catch(Exception e) {
//			
//		}
//	}

	// 가계부 내역 추가
	public void insertAccountBook(HttpServletRequest req, Model model) {
		// 로그인한 아이디 받아오기
		String member_id = (String) req.getSession().getAttribute("customerID");

		AccountBookVO vo = new AccountBookVO();
		vo.setMember_id(member_id);
		vo.setIncome(Integer.parseInt(req.getParameter("income")));
		vo.setExpense(Integer.parseInt(req.getParameter("expense")));
		vo.setRegister_date(req.getParameter("register_date"));

		int insertCnt = dao.insertAccountBook(vo);
		System.out.println("가계부내역추가 insertCnt : " + insertCnt);
		model.addAttribute("insertCnt", insertCnt);
	}

	// 가계부 내역 삭제
	public void deleteAccountBook(HttpServletRequest req, Model model) {
		// 로그인한 아이디 받아오기
		String member_id = (String) req.getSession().getAttribute("customerID");
		AccountBookVO vo = new AccountBookVO();
		vo.setMember_id(member_id);
		vo.setRegister_date(req.getParameter("register_date"));

		int deleteCnt = dao.deleteAccountBook(vo);
		System.out.println("가계부내역삭제 deleteCnt : " + deleteCnt);
		model.addAttribute("deleteCnt", deleteCnt);
	}

	// 가계부 내역 조회
	public void getAccountBook(HttpServletRequest req, Model model) {
		// 로그인한 아이디 받아오기
		String member_id = (String) req.getSession().getAttribute("customerID");
		ArrayList<AccountBookVO> list = dao.getAccountBook(member_id);
		ArrayList<AccountBookVO> report = dao.getAccountBookReport(member_id);
		ArrayList<AccountBookVO> auto = dao.myAccountAutoTransfer(member_id);
		System.out.println("report 사이즈 : " + report.size());
		int sumAutoMoney = 0;
		for (int i = 0; i < auto.size(); i++) {
			sumAutoMoney += auto.get(i).getAuto_money();
		}
		System.out.println("해당날짜 자동이체 합계액 :" + sumAutoMoney);
		model.addAttribute("report", report);
		model.addAttribute("list", list);
		model.addAttribute("auto", auto);
		model.addAttribute("sumAutoMoney", sumAutoMoney);

	}

	// 예금 리스트(민재)
	@Override
	public void myDepositList(HttpServletRequest req, Model model) {
		System.out.println("[보유상품목록 => 예금화면]");

		String strId = (String) req.getSession().getAttribute("customerID");
		// strId = "kim";
		System.out.println("strId : " + strId);

		// 회원 이름 가져오기
		String member_name = dao.getName(strId);
		System.out.println("member_name : " + member_name);

		req.setAttribute("member_name", member_name);
		req.setAttribute("boardName", "예금");
	}

	// 예금서브 리스트(민재)
	@Override
	public void myDepositSubList(HttpServletRequest req, Model model) {
		System.out.println("[보유상품목록 => 예금서브리스트]");

		String strId = (String) req.getSession().getAttribute("customerID");
		// strId = "kim";
		System.out.println("strId : " + strId);

		int selectValue = Integer.parseInt(req.getParameter("select"));
		System.out.println("selectValue : " + selectValue);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("member_id", strId);
		map.put("account_type", 1);

		List<MyDepositVO> list;
		// 리스트 가져오기
		if (selectValue == 0) {
			list = dao.depositList(map);
		} else {
			map.put("account_bankCode", selectValue);
			list = dao.depositSubList(map);
		}

		int cnt = list.size();
		System.out.println("cnt : " + cnt);
		System.out.println("list : " + list);
		System.out.println("서브리스트");
		req.setAttribute("boardName", "예금");
		req.setAttribute("list", list);
		req.setAttribute("cnt", cnt);
	}

	// 적금 리스트(민재)
	@Override
	public void mySavingList(HttpServletRequest req, Model model) {
		System.out.println("[보유상품목록 => 적금화면]");

		String strId = (String) req.getSession().getAttribute("customerID");
		// strId = "kim";
		System.out.println("strId : " + strId);

		// 회원 이름 가져오기
		String member_name = dao.getName(strId);
		System.out.println("member_name : " + member_name);

		req.setAttribute("member_name", member_name);
		req.setAttribute("boardName", "적금");
	}

	// 적금서브 리스트(민재)
	@Override
	public void mySavingSubList(HttpServletRequest req, Model model) {
		System.out.println("[보유상품목록 => 적금서브리스트]");

		String strId = (String) req.getSession().getAttribute("customerID");
		// strId = "kim";
		System.out.println("strId : " + strId);

		int selectValue = Integer.parseInt(req.getParameter("select"));
		System.out.println("selectValue : " + selectValue);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("member_id", strId);
		map.put("account_type", 2);

		List<MySavingVO> list;
		// 리스트 가져오기
		if (selectValue == 0) {
			list = dao.savingList(map);
		} else {
			map.put("account_bankCode", selectValue);
			list = dao.savingSubList(map);
		}

		int cnt = list.size();
		System.out.println("cnt : " + cnt);
		System.out.println("list : " + list);
		System.out.println("서브리스트");
		req.setAttribute("boardName", "적금");
		req.setAttribute("list", list);
		req.setAttribute("cnt", cnt);
	}

	// 연금 리스트(민재)
	@Override
	public void myIrpList(HttpServletRequest req, Model model) {
		System.out.println("[보유상품목록 => 연금화면]");

		String strId = (String) req.getSession().getAttribute("customerID");
		// strId = "kim";
		System.out.println("strId : " + strId);

		// 회원 이름 가져오기
		String member_name = dao.getName(strId);
		System.out.println("member_name : " + member_name);

		req.setAttribute("member_name", member_name);
		req.setAttribute("boardName", "연금");

		List<MyIRPVO> list = null;
		// 리스트 가져오기
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("member_id", strId);
		map.put("account_type", 3);

		list = dao.irpList(map);

		int cnt = list.size();
		System.out.println("cnt : " + cnt);
		System.out.println("list : " + list);
		System.out.println("서브리스트");
		req.setAttribute("boardName", "연금");
		req.setAttribute("list", list);
		req.setAttribute("cnt", cnt);
	}

	// 공지사항리스트(민재)
	@Override
	public void noticeList(HttpServletRequest req, Model model) {
		System.out.println("[고객센터목록 => 공지사항리스트]");

		// 3단계. 화면으로부터 입력받은 값을 받아온다.
		// 페이징
		int pageSize = 10; // 한 페이지당 출력할 글 갯수
		int pageBlock = 5; // 한 블럭당 페이지 갯수

		int cnt = 0; // 글 갯수
		int start = 0; // 현재 페이지 시작 글번호
		int end = 0; // 현재 페이지 마지막 글번호
		int number = 0; // 출력용 글번호
		String pageNum = ""; // 페이지 번호
		int currentPage = 0; // 현재 페이지

		int pageCount = 0; // 페이지 갯수
		int startPage = 0; // 시작 페이지
		int endPage = 0; // 마지막 페이지

		// 4단계. 다형성 적용, 싱글톤 방식으로 dao 객체 생성

		// 5-1단계. 게시글 갯수 조회
		cnt = dao.getNoticeCnt();
		System.out.println("cnt ==> " + cnt);

		pageNum = req.getParameter("pageNum");

		if (pageNum == null) {
			pageNum = "1"; // 첫 페이지를 1페이지로 지정
		}

		// 글 30건 기준
		currentPage = Integer.parseInt(pageNum);
		System.out.println("currentPage : " + currentPage);

		// 페이지 갯수 // 6페이지 = (30건 / 한 페이지당 5건 ) + (나머지 : 0)
		pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지갯수 + 나머지 있으면 1페이지

		// 현재페이지 시작 글번호(페이지별)
		// start = (currentPage - 1) * pageSize + 1;
		// 1 = (1 - 1) * 5 + 1
		start = (currentPage - 1) * pageSize + 1;

		// 현재페이지 마지막 글번호(페이지별)
		// end = start + pageSize - 1;
		// 5 = 1 + 5 - 1
		end = start + pageSize - 1;
		System.out.println("start : " + start);
		System.out.println("end : " + end);

		// 출력용 글번호
		// 30 = 30 - (1 - 1) * 5;
		// number = cnt - (currentPage - 1) * pageSize;
		number = cnt - (currentPage - 1) * pageSize;
		System.out.println("number : " + number);
		System.out.println("pageSize : " + pageSize);

		// 시작 페이지
		// 1 = (1 / 3) * 3 + 1;
		// startPage = (currentPage / pageBlock) * pageBlock + 1;
		startPage = (currentPage / pageBlock) * pageBlock + 1;
		if (currentPage % pageBlock == 0)
			startPage -= pageBlock;
		System.out.println("starPage : " + startPage);

		// 마지막 페이지
		// 3 = 1 + 3 - 1;
		endPage = startPage + pageBlock - 1;
		if (endPage > pageCount)
			endPage = pageCount;

		System.out.println("endPage : " + endPage);

		System.out.println("================================");

		List<NoticeVO> list = null;

		if (cnt > 0) {
			// 5-2단계. 게시글 목록 조회
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("start", start);
			map.put("end", end);
			list = dao.getNoticeList(map);
			System.out.println("list : " + list);
		}

		// 6단계. jsp로 전달하기 위해 request나 session에 처리결과를 저장
		req.setAttribute("list", list); // 게시글목록
		req.setAttribute("cnt", cnt); // 글갯수
		req.setAttribute("pageNum", pageNum); // 페이지번호
		req.setAttribute("number", number); // 출력용 글번호

		if (cnt > 0) {
			req.setAttribute("startPage", startPage); // 시작페이지
			req.setAttribute("endPage", endPage); // 마지막페이지
			req.setAttribute("pageBlock", pageBlock); // 한 블럭당 페이지 갯수
			req.setAttribute("pageCount", pageCount); // 페이지 갯수
			req.setAttribute("currentPage", currentPage); // 현재 페이지
		}
	}

	// 계좌 생성 Method
	public String createAccountId(int account_type) {
		String account_id = "";

		if (account_type == 1) {
			// 국민(14) > 6 - 2 - 6
			String st1 = String.format("%06d", (int) (Math.random() * 1000000));
			String st2 = String.format("%02d", (int) (Math.random() * 100));
			String st3 = String.format("%06d", (int) (Math.random() * 1000000));

			account_id = st1 + "-" + st2 + "-" + st3;

			System.out.println("account_id : " + account_id);

		} else if (account_type == 2) {
			// 우리(13) > 4 - 3 - 6
			String st1 = String.format("%04d", (int) (Math.random() * 10000));
			String st2 = String.format("%03d", (int) (Math.random() * 1000));
			String st3 = String.format("%06d", (int) (Math.random() * 1000000));

			account_id = st1 + "-" + st2 + "-" + st3;

			System.out.println("account_id : " + account_id);

		} else if (account_type == 3) {
			// 농협(13) > 3 - 4 - 4 - 2
			String st1 = String.format("%03d", (int) (Math.random() * 1000));
			String st2 = String.format("%04d", (int) (Math.random() * 10000));
			String st3 = String.format("%04d", (int) (Math.random() * 10000));
			String st4 = String.format("%02d", (int) (Math.random() * 100));

			account_id = st1 + "-" + st2 + "-" + st3 + "-" + st4;

			System.out.println("account_id : " + account_id);

		} else if (account_type == 4) {
			// 신한(12) > 3 - 3 - 6
			String st1 = String.format("%03d", (int) (Math.random() * 1000));
			String st2 = String.format("%03d", (int) (Math.random() * 1000));
			String st3 = String.format("%06d", (int) (Math.random() * 1000000));

			account_id = st1 + "-" + st2 + "-" + st3;

			System.out.println("account_id : " + account_id);

		} else if (account_type == 5) {
			// 하나(14) > 3 - 6 - 5
			String st1 = String.format("%03d", (int) (Math.random() * 1000));
			String st2 = String.format("%06d", (int) (Math.random() * 1000000));
			String st3 = String.format("%05d", (int) (Math.random() * 100000));

			account_id = st1 + "-" + st2 + "-" + st3;

			System.out.println("account_id : " + account_id);

		} else if (account_type == 6) {
			// 코스모뱅크(14) > 4 - 4 - 6
			String st1 = String.format("%04d", (int) (Math.random() * 10000));
			String st2 = String.format("%04d", (int) (Math.random() * 10000));
			String st3 = String.format("%06d", (int) (Math.random() * 1000000));

			account_id = st1 + "-" + st2 + "-" + st3;

			System.out.println("account_id : " + account_id);
		}
		return account_id;
	}

	// 공지사항 상세페이지(민재)
	@Override
	public void noticeDetailAction(HttpServletRequest req, Model model) {
		System.out.println("[고객센터목록 => 공지사항상세페이지]");

		// 화면으로부터 값 받아오기(get방식)
		int notice_num = Integer.parseInt(req.getParameter("notice_num"));
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int number = Integer.parseInt(req.getParameter("number"));

		String id = (String) req.getSession().getAttribute("customerID");

		// 조회수증가(관리자 조회수파악용) => 관리자는 타면 안된다.
		if (id != null) {
			dao.addNoticeReadCnt(notice_num);
		}

		// 게시글 상세조회
		NoticeVO vo = dao.getNoticeDetail(notice_num);

		// jsp로 전송
		req.setAttribute("vo", vo);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("number", number);
	}

	//대출 납입 목록 (지현)
	   @Override
	   public void loanHistoryList(HttpServletRequest req, Model model) { // 지은
	      System.out.println("[AdminService => loanHistoryList()]");
	      String member_id = (String)req.getSession().getAttribute("customerID");
	      // 페이징
	      int pageSize = 5; // 한 페이지당 출력할 글 갯수
	      int pageBlock = 3; // 한 블럭당 페이지 갯수

	      int cnt = 0; // 글 갯수
	      int start = 0; // 현재 페이지 시작 글 번호
	      int end = 0; // 현재 페이지 마지막 글 번호
	      int number = 0; // 출력용 글번호
	      String pageNum = ""; // 페이지 번호
	      int currentPage = 0; // 현재 페이지

	      int pageCount = 0; // 페이지 갯수
	      int startPage = 0; // 시작 페이지
	      int endPage = 0; // 마지막 페이지

	      pageNum = req.getParameter("pageNum");

	      if (pageNum == null) {
	      pageNum = "1"; // 첫 페이지를 1페이지로 지정
	      }

	      System.out.println("customerID => " + member_id);
	      cnt = dao.getLoanHistoryCnt(member_id);
	      System.out.println("cnt : " + cnt);

	      // 글 30건 기준
	      currentPage = Integer.parseInt(pageNum);
	      System.out.println("currentPage : " + currentPage);

	      // 페이지 갯수 6= (30/5) + (0)
	      pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지가 있으면 1페이지 추가

	      // 현재페이지 시작 글번호(페이지별)
	      // start = (currentPage - 1) * pageSize + 1;
	      // 1 = (1-1) * 5 + 1
	      start = (currentPage - 1) * pageSize + 1;

	      // 현재페이지 마지막 글번호(페이지별)
	      // end = start + pageSize - 1;
	      // 5 = 1 + 5 - 1
	      end = start + pageSize - 1;

	      System.out.println("start : " + start);
	      System.out.println("end : " + end);

	      // 출력용 글번호
	      // 30 = 30 - (1 - 1) * 5; // 1페이지
	      // number = cnt - (currentPage - 1) * pageSize;
	      number = cnt - (currentPage - 1) * pageSize;

	      System.out.println("number : " + number);
	      System.out.println("pageSize : " + pageSize);

	      // 시작 페이지
	      // 1 = (1 / 3) * 3 + 1;
	      // startPage = (currentPage / pageBlock) * pageBlock + 1;
	      startPage = (currentPage / pageBlock) * pageBlock + 1;
	      if (currentPage % pageBlock == 0)
	      startPage -= pageBlock;

	      System.out.println("startPage : " + startPage);

	      // 마지막 페이지
	      // 3 = 1 + 3 - 1
	      endPage = startPage + pageBlock - 1;
	      if (endPage > pageCount)
	      endPage = pageCount;

	      System.out.println("endPage : " + endPage);

	      System.out.println("==============================================");

	      ArrayList<LoanHistoryVO> loanHistorys = null;

	      Map<String, Object> map = new HashMap<String, Object>();
	      map.put("start", start);
	      map.put("end", end);
	      map.put("member_id", member_id);

	      if (cnt > 0) {
	      // 5-2 게시글 목록 조회
	      loanHistorys = dao.getLoanHistoryList(map);
	      }

	      // 6단계. jsp로 전달하기 위해 request나 session에 처리결과를 저장
	      model.addAttribute("loanHistorys", loanHistorys); // 게시글 목록
	      model.addAttribute("cnt", cnt); // 게시글 갯수
	      model.addAttribute("pageNum", pageNum); // 페이지 번호
	      model.addAttribute("number", number); // 출력용 글번호

	      if (cnt > 0) {
	      model.addAttribute("startPage", startPage); // 시작페이지
	      model.addAttribute("endPage", endPage); // 마지막페이지
	      model.addAttribute("pageBlock", pageBlock); // 한 블럭당 페이지 갯수
	      model.addAttribute("pageCount", pageCount); // 페이지 갯수
	      model.addAttribute("currentPage", currentPage); // 현재페이지
	      }
	      }

	   
	   
	   
	   public void loanCancelList(HttpServletRequest req, Model model) { // 지은
	      System.out.println("[UserService => loanCancelList()]");
	      // 페이징
	      int pageSize = 5; // 한 페이지당 출력할 글 갯수
	      int pageBlock = 3; // 한 블럭당 페이지 갯수

	      int cnt = 0; // 글 갯수
	      int start = 0; // 현재 페이지 시작 글 번호
	      int end = 0; // 현재 페이지 마지막 글 번호
	      int number = 0; // 출력용 글번호
	      String pageNum = ""; // 페이지 번호
	      int currentPage = 0; // 현재 페이지

	      int pageCount = 0; // 페이지 갯수
	      int startPage = 0; // 시작 페이지
	      int endPage = 0; // 마지막 페이지

	      pageNum = req.getParameter("pageNum");

	      if (pageNum == null) {
	         pageNum = "1"; // 첫 페이지를 1페이지로 지정
	      }

	      cnt = dao.getLoanCancelCnt((String) req.getSession().getAttribute("customerID"));
	      System.out.println("cnt : " + cnt);

	      // 글 30건 기준
	      currentPage = Integer.parseInt(pageNum);
	      System.out.println("currentPage : " + currentPage);

	      // 페이지 갯수 6= (30/5) + (0)
	      pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지가 있으면 1페이지 추가

	      // 현재페이지 시작 글번호(페이지별)
	      // start = (currentPage - 1) * pageSize + 1;
	      // 1 = (1-1) * 5 + 1
	      start = (currentPage - 1) * pageSize + 1;

	      // 현재페이지 마지막 글번호(페이지별)
	      // end = start + pageSize - 1;
	      // 5 = 1 + 5 - 1
	      end = start + pageSize - 1;

	      System.out.println("start : " + start);
	      System.out.println("end : " + end);

	      // 출력용 글번호
	      // 30 = 30 - (1 - 1) * 5; // 1페이지
	      // number = cnt - (currentPage - 1) * pageSize;
	      number = cnt - (currentPage - 1) * pageSize;

	      System.out.println("number : " + number);
	      System.out.println("pageSize : " + pageSize);

	      // 시작 페이지
	      // 1 = (1 / 3) * 3 + 1;
	      // startPage = (currentPage / pageBlock) * pageBlock + 1;
	      startPage = (currentPage / pageBlock) * pageBlock + 1;
	      if (currentPage % pageBlock == 0)
	         startPage -= pageBlock;

	      System.out.println("startPage : " + startPage);

	      // 마지막 페이지
	      // 3 = 1 + 3 - 1
	      endPage = startPage + pageBlock - 1;
	      if (endPage > pageCount)
	         endPage = pageCount;

	      System.out.println("endPage : " + endPage);

	      System.out.println("==============================================");

	      ArrayList<LoanVO> loans = null;

	      Map<String, Object> map = new HashMap<String, Object>();
	      map.put("start", start);
	      map.put("end", end);
	      map.put("member_id", req.getSession().getAttribute("customerID"));

	      if (cnt > 0) {
	         // 5-2 게시글 목록 조회
	         loans = dao.getLoanCancelList(map);
	         System.out.println(loans);
	      }

	      // 6단계. jsp로 전달하기 위해 request나 session에 처리결과를 저장
	      model.addAttribute("loans", loans); // 게시글 목록
	      model.addAttribute("cnt", cnt); // 게시글 갯수
	      model.addAttribute("pageNum", pageNum); // 페이지 번호
	      model.addAttribute("number", number); // 출력용 글번호

	      if (cnt > 0) {
	         model.addAttribute("startPage", startPage); // 시작페이지
	         model.addAttribute("endPage", endPage); // 마지막페이지
	         model.addAttribute("pageBlock", pageBlock); // 한 블럭당 페이지 갯수
	         model.addAttribute("pageCount", pageCount); // 페이지 갯수
	         model.addAttribute("currentPage", currentPage); // 현재페이지
	      }

	   }

	   public void loanList(HttpServletRequest req, Model model) { // 지은
	      System.out.println("[UserService => loanList()]");
	      // 페이징
	      int pageSize = 5; // 한 페이지당 출력할 글 갯수
	      int pageBlock = 3; // 한 블럭당 페이지 갯수

	      int cnt = 0; // 글 갯수
	      int start = 0; // 현재 페이지 시작 글 번호
	      int end = 0; // 현재 페이지 마지막 글 번호
	      int number = 0; // 출력용 글번호
	      String pageNum = ""; // 페이지 번호
	      int currentPage = 0; // 현재 페이지

	      int pageCount = 0; // 페이지 갯수
	      int startPage = 0; // 시작 페이지
	      int endPage = 0; // 마지막 페이지
	      String member_id = (String) req.getSession().getAttribute("customerID");
	      
	      pageNum = req.getParameter("pageNum");

	      if (pageNum == null) {
	         pageNum = "1"; // 첫 페이지를 1페이지로 지정
	      }

	      Map<String, Object> map = new HashMap<String, Object>();
	      map.put("member_id", member_id);
	      cnt = dao.getLoanCnt(map);
	      System.out.println("cnt : " + cnt);

	      // 글 30건 기준
	      currentPage = Integer.parseInt(pageNum);
	      System.out.println("currentPage : " + currentPage);

	      // 페이지 갯수 6= (30/5) + (0)
	      pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지가 있으면 1페이지 추가

	      // 현재페이지 시작 글번호(페이지별)
	      // start = (currentPage - 1) * pageSize + 1;
	      // 1 = (1-1) * 5 + 1
	      start = (currentPage - 1) * pageSize + 1;

	      // 현재페이지 마지막 글번호(페이지별)
	      // end = start + pageSize - 1;
	      // 5 = 1 + 5 - 1
	      end = start + pageSize - 1;

	      System.out.println("start : " + start);
	      System.out.println("end : " + end);

	      // 출력용 글번호
	      // 30 = 30 - (1 - 1) * 5; // 1페이지
	      // number = cnt - (currentPage - 1) * pageSize;
	      number = cnt - (currentPage - 1) * pageSize;

	      System.out.println("number : " + number);
	      System.out.println("pageSize : " + pageSize);

	      // 시작 페이지
	      // 1 = (1 / 3) * 3 + 1;
	      // startPage = (currentPage / pageBlock) * pageBlock + 1;
	      startPage = (currentPage / pageBlock) * pageBlock + 1;
	      if (currentPage % pageBlock == 0)
	         startPage -= pageBlock;

	      System.out.println("startPage : " + startPage);

	      // 마지막 페이지
	      // 3 = 1 + 3 - 1
	      endPage = startPage + pageBlock - 1;
	      if (endPage > pageCount)
	         endPage = pageCount;

	      System.out.println("endPage : " + endPage);

	      System.out.println("==============================================");

	      ArrayList<LoanProductVO> loans = null;

	      map.put("start", start);
	      map.put("end", end);

	      if (cnt > 0) {
	         // 5-2 게시글 목록 조회
	         loans = dao.getLoanList(map);
	      }

	      // 6단계. jsp로 전달하기 위해 request나 session에 처리결과를 저장
	      model.addAttribute("loans", loans); // 게시글 목록
	      model.addAttribute("cnt", cnt); // 게시글 갯수
	      model.addAttribute("pageNum", pageNum); // 페이지 번호
	      model.addAttribute("number", number); // 출력용 글번호

	      if (cnt > 0) {
	         model.addAttribute("startPage", startPage); // 시작페이지
	         model.addAttribute("endPage", endPage); // 마지막페이지
	         model.addAttribute("pageBlock", pageBlock); // 한 블럭당 페이지 갯수
	         model.addAttribute("pageCount", pageCount); // 페이지 갯수
	         model.addAttribute("currentPage", currentPage); // 현재페이지
	      }

	   }

	   public void loanProductList(HttpServletRequest req, Model model) { // 지은
	      System.out.println("[AdminService => loanProductList()]");

	      // 페이징
	      int pageSize = 5; // 한 페이지당 출력할 글 갯수
	      int pageBlock = 3; // 한 블럭당 페이지 갯수

	      int cnt = 0; // 글 갯수
	      int start = 0; // 현재 페이지 시작 글 번호
	      int end = 0; // 현재 페이지 마지막 글 번호
	      int number = 0; // 출력용 글번호
	      String pageNum = ""; // 페이지 번호
	      int currentPage = 0; // 현재 페이지

	      int pageCount = 0; // 페이지 갯수
	      int startPage = 0; // 시작 페이지
	      int endPage = 0; // 마지막 페이지

	      pageNum = req.getParameter("pageNum");

	      if (pageNum == null) {
	         pageNum = "1"; // 첫 페이지를 1페이지로 지정
	      }

	      cnt = dao.getLoanProductCnt();
	      System.out.println("cnt : " + cnt);

	      // 글 30건 기준
	      currentPage = Integer.parseInt(pageNum);
	      System.out.println("currentPage : " + currentPage);

	      // 페이지 갯수 6= (30/5) + (0)
	      pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지가 있으면 1페이지 추가

	      // 현재페이지 시작 글번호(페이지별)
	      // start = (currentPage - 1) * pageSize + 1;
	      // 1 = (1-1) * 5 + 1
	      start = (currentPage - 1) * pageSize + 1;

	      // 현재페이지 마지막 글번호(페이지별)
	      // end = start + pageSize - 1;
	      // 5 = 1 + 5 - 1
	      end = start + pageSize - 1;

	      System.out.println("start : " + start);
	      System.out.println("end : " + end);

	      // 출력용 글번호
	      // 30 = 30 - (1 - 1) * 5; // 1페이지
	      // number = cnt - (currentPage - 1) * pageSize;
	      number = cnt - (currentPage - 1) * pageSize;

	      System.out.println("number : " + number);
	      System.out.println("pageSize : " + pageSize);

	      // 시작 페이지
	      // 1 = (1 / 3) * 3 + 1;
	      // startPage = (currentPage / pageBlock) * pageBlock + 1;
	      startPage = (currentPage / pageBlock) * pageBlock + 1;
	      if (currentPage % pageBlock == 0)
	         startPage -= pageBlock;

	      System.out.println("startPage : " + startPage);

	      // 마지막 페이지
	      // 3 = 1 + 3 - 1
	      endPage = startPage + pageBlock - 1;
	      if (endPage > pageCount)
	         endPage = pageCount;

	      System.out.println("endPage : " + endPage);

	      System.out.println("==============================================");

	      ArrayList<LoanProductVO> loanProducts = null;

	      Map<String, Object> map = new HashMap<String, Object>();
	      map.put("start", start);
	      map.put("end", end);

	      if (cnt > 0) {
	         // 5-2 게시글 목록 조회
	         loanProducts = dao.getLoanProductList(map);
	      }

	      // 6단계. jsp로 전달하기 위해 request나 session에 처리결과를 저장
	      model.addAttribute("loanProducts", loanProducts); // 게시글 목록
	      model.addAttribute("cnt", cnt); // 게시글 갯수
	      model.addAttribute("pageNum", pageNum); // 페이지 번호
	      model.addAttribute("number", number); // 출력용 글번호

	      if (cnt > 0) {
	         model.addAttribute("startPage", startPage); // 시작페이지
	         model.addAttribute("endPage", endPage); // 마지막페이지
	         model.addAttribute("pageBlock", pageBlock); // 한 블럭당 페이지 갯수
	         model.addAttribute("pageCount", pageCount); // 페이지 갯수
	         model.addAttribute("currentPage", currentPage); // 현재페이지
	      }

	   }

	   public void searchLoanProductList(HttpServletRequest req, Model model) { // 지은
	      System.out.println("[AdminService => loanProductList()]");

	      // 페이징
	      int pageSize = 5; // 한 페이지당 출력할 글 갯수
	      int pageBlock = 3; // 한 블럭당 페이지 갯수

	      int cnt = 0; // 글 갯수
	      int start = 0; // 현재 페이지 시작 글 번호
	      int end = 0; // 현재 페이지 마지막 글 번호
	      int number = 0; // 출력용 글번호
	      String pageNum = ""; // 페이지 번호
	      int currentPage = 0; // 현재 페이지

	      int pageCount = 0; // 페이지 갯수
	      int startPage = 0; // 시작 페이지
	      int endPage = 0; // 마지막 페이지
	      String keyword = (String) req.getParameter("keyword");

	      pageNum = req.getParameter("pageNum");

	      if (pageNum == null) {
	         pageNum = "1"; // 첫 페이지를 1페이지로 지정
	      }

	      cnt = dao.getSearchLoanProductCnt(keyword);
	      System.out.println("cnt : " + cnt);

	      // 글 30건 기준
	      currentPage = Integer.parseInt(pageNum);
	      System.out.println("currentPage : " + currentPage);

	      // 페이지 갯수 6= (30/5) + (0)
	      pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지가 있으면 1페이지 추가

	      // 현재페이지 시작 글번호(페이지별)
	      // start = (currentPage - 1) * pageSize + 1;
	      // 1 = (1-1) * 5 + 1
	      start = (currentPage - 1) * pageSize + 1;

	      // 현재페이지 마지막 글번호(페이지별)
	      // end = start + pageSize - 1;
	      // 5 = 1 + 5 - 1
	      end = start + pageSize - 1;

	      System.out.println("start : " + start);
	      System.out.println("end : " + end);

	      // 출력용 글번호
	      // 30 = 30 - (1 - 1) * 5; // 1페이지
	      // number = cnt - (currentPage - 1) * pageSize;
	      number = cnt - (currentPage - 1) * pageSize;

	      System.out.println("number : " + number);
	      System.out.println("pageSize : " + pageSize);

	      // 시작 페이지
	      // 1 = (1 / 3) * 3 + 1;
	      // startPage = (currentPage / pageBlock) * pageBlock + 1;
	      startPage = (currentPage / pageBlock) * pageBlock + 1;
	      if (currentPage % pageBlock == 0)
	         startPage -= pageBlock;

	      System.out.println("startPage : " + startPage);

	      // 마지막 페이지
	      // 3 = 1 + 3 - 1
	      endPage = startPage + pageBlock - 1;
	      if (endPage > pageCount)
	         endPage = pageCount;

	      System.out.println("endPage : " + endPage);

	      System.out.println("==============================================");

	      ArrayList<LoanProductVO> loanProducts = null;

	      Map<String, Object> map = new HashMap<String, Object>();
	      map.put("start", start);
	      map.put("end", end);
	      map.put("keyword", keyword);

	      if (cnt > 0) {
	         // 5-2 게시글 목록 조회
	         loanProducts = dao.searchLoanProductList(map);
	      }

	      // 6단계. jsp로 전달하기 위해 request나 session에 처리결과를 저장
	      model.addAttribute("loanProducts", loanProducts); // 게시글 목록
	      model.addAttribute("cnt", cnt); // 게시글 갯수
	      model.addAttribute("pageNum", pageNum); // 페이지 번호
	      model.addAttribute("number", number); // 출력용 글번호
	      model.addAttribute("keyword", keyword); // keyword

	      if (cnt > 0) {
	         model.addAttribute("startPage", startPage); // 시작페이지
	         model.addAttribute("endPage", endPage); // 마지막페이지
	         model.addAttribute("pageBlock", pageBlock); // 한 블럭당 페이지 갯수
	         model.addAttribute("pageCount", pageCount); // 페이지 갯수
	         model.addAttribute("currentPage", currentPage); // 현재페이지
	      }

	   }

	   public void newLoanDetail(HttpServletRequest req, Model model) { // 지은
	      String loan_product_name = req.getParameter("loan_product_name");

	      LoanProductVO loanProduct = dao.getLoanProductInfo(loan_product_name);

	      model.addAttribute("loanProduct", loanProduct);
	      model.addAttribute("loan_product_name", loan_product_name);
	   }

	   public void newLoanSign(HttpServletRequest req, Model model) { // 지은
	      String loan_product_name = req.getParameter("loan_product_name");

	      LoanProductVO loanProduct = dao.getLoanProductInfo(loan_product_name);
	      UserVO user = dao.getUserInfo((String) req.getSession().getAttribute("customerID"));

	      model.addAttribute("loanProduct", loanProduct);
	      model.addAttribute("user", user);
	      model.addAttribute("loan_product_name", loan_product_name);
	   }

	   

	   public void signInfo(HttpServletRequest req, Model model) { // 지은
	      String loan_product_name = (String) req.getParameter("loan_product_name");
	      String member_id = (String) req.getSession().getAttribute("customerID");

	      Map<String, Object> map = new HashMap<String, Object>();
	      map.put("member_id", member_id);

	      UserVO loanMember = dao.getUserInfo(member_id);
	      LoanProductVO loanProduct = dao.getLoanProductInfo(loan_product_name);
	      
	      
	      model.addAttribute("loanMember", loanMember);
	      model.addAttribute("loanProduct", loanProduct);
	   }


	      
	      
	   //신규대출신청 insert
	   public void newLoanSignAction(HttpServletRequest req, Model model) throws ParseException {
	      String loan_product_name = (String) req.getParameter("loan_product_name");
	      String member_id = (String) req.getParameter("member_id");
	      String account_id = req.getParameter("account_id");
	      int loan_state = 1; // final static int선언해야됨. 1:신청
	      
	      DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
	      String str_loan_startDate = (String) req.getParameter("loan_startDate");
	      Date loan_startDate = Date.valueOf(LocalDate.parse(str_loan_startDate, format));
	      String str_loan_endDate = (String) req.getParameter("loan_endDate");
	      Date loan_endDate = Date.valueOf(LocalDate.parse(str_loan_endDate, format));
	      
	      int loan_month = Integer.parseInt((String) req.getParameter("loan_month"));
	      int loan_repaymentType = Integer.parseInt((String) req.getParameter("loan_repaymentType"));
	      float loan_rate = Float.parseFloat((String) req.getParameter("loan_rate"));
	      int loan_monthlyRepayment = Integer.parseInt((String) req.getParameter("loan_monthlyRepayment"));
	      int loan_amount = Integer.parseInt((String) req.getParameter("loan_amount"));
	      int loan_balance = loan_amount;
	      int loan_interest = Integer.parseInt((String) req.getParameter("loan_interest"));
	      int loan_tranAmount = Integer.parseInt((String) req.getParameter("loan_tranAmount"));
	      int loan_tranInterest = Integer.parseInt((String) req.getParameter("loan_tranInterest"));
	      int loan_delinquency = 0;
	      float loan_prepaymentRate = Float.parseFloat((String) req.getParameter("loan_prepaymentRate"));
	      
	      LoanVO loan = new LoanVO();
	      loan.setLoan_product_name(loan_product_name);
	      loan.setMember_id(member_id);
	      loan.setAccount_id(account_id);
	      loan.setLoan_state(loan_state);
	      loan.setLoan_startDate(loan_startDate);
	      loan.setLoan_endDate(loan_endDate);
	      loan.setLoan_month(loan_month);
	      loan.setLoan_repaymentType(loan_repaymentType);
	      loan.setLoan_rate(loan_rate);
	      loan.setLoan_monthlyRepayment(loan_monthlyRepayment);
	      loan.setLoan_amount(loan_amount);
	      loan.setLoan_balance(loan_balance);
	      loan.setLoan_interest(loan_interest);
	      loan.setLoan_tranAmount(loan_tranAmount);
	      loan.setLoan_tranInterest(loan_tranInterest);
	      loan.setLoan_delinquency(loan_delinquency);
	      loan.setLoan_prepaymentRate(loan_prepaymentRate);
	      
	      int insertCnt = dao.newLoanSignAction(loan);
	      model.addAttribute("insertCnt", insertCnt);
	   }

	   public void loanPrincipalRateList(HttpServletRequest req, Model model) { // 지은
	      String loan_id = (String) req.getParameter("loan_id");

	      Map<String, Object> map = new HashMap<String, Object>();
	      map.put("loan_id", loan_id);
	      
	      LoanVO loan = dao.getLoanInfo(map);
	      System.out.println(loan);
	      req.setAttribute("loan", loan);
	   }
	   
	   // 나의 보험 상환 내역_loan_id별  (지현)
	   @Override
	   public void myLoanList(HttpServletRequest req, Model model) {
	      System.out.println("myloanListService");
	      int loan_id = Integer.parseInt(req.getParameter("loan_id"));
	      String member_id = (String)req.getSession().getAttribute("customerID");
	      
	      int pageSize = 5; // 한 페이지당 출력할 글 갯수
	      int pageBlock = 3; // 한 블럭당 페이지 갯수

	      int cnt = 0; // 글 갯수
	      int start = 0; // 현재 페이지 시작 글 번호
	      int end = 0; // 현재 페이지 마지막 글 번호
	      int number = 0; // 출력용 글번호
	      String pageNum = ""; // 페이지 번호
	      int currentPage = 0; // 현재 페이지

	      int pageCount = 0; // 페이지 갯수
	      int startPage = 0; // 시작 페이지
	      int endPage = 0; // 마지막 페이지

	      pageNum = req.getParameter("pageNum");

	      if (pageNum == null) {
	      pageNum = "1"; // 첫 페이지를 1페이지로 지정
	      }
	      
	      Map<String, Object> map = new HashMap<String, Object>();
	      map.put("member_id", member_id);
	      map.put("loan_id", loan_id);
	      
	      cnt = dao.getLoanPayCnt(map);
	      System.out.println("cnt : " + cnt);

	      // 글 30건 기준
	      currentPage = Integer.parseInt(pageNum);
	      System.out.println("currentPage : " + currentPage);

	      // 페이지 갯수 6= (30/5) + (0)
	      pageCount = (cnt / pageSize) + (cnt % pageSize > 0 ? 1 : 0); // 페이지 갯수 + 나머지가 있으면 1페이지 추가

	      // 현재페이지 시작 글번호(페이지별)
	      // start = (currentPage - 1) * pageSize + 1;
	      // 1 = (1-1) * 5 + 1
	      start = (currentPage - 1) * pageSize + 1;

	      // 현재페이지 마지막 글번호(페이지별)
	      // end = start + pageSize - 1;
	      // 5 = 1 + 5 - 1
	      end = start + pageSize - 1;

	      System.out.println("start : " + start);
	      System.out.println("end : " + end);

	      // 출력용 글번호
	      // 30 = 30 - (1 - 1) * 5; // 1페이지
	      // number = cnt - (currentPage - 1) * pageSize;
	      number = cnt - (currentPage - 1) * pageSize;

	      System.out.println("number : " + number);
	      System.out.println("pageSize : " + pageSize);

	      // 시작 페이지
	      // 1 = (1 / 3) * 3 + 1;
	      // startPage = (currentPage / pageBlock) * pageBlock + 1;
	      startPage = (currentPage / pageBlock) * pageBlock + 1;
	      if (currentPage % pageBlock == 0)
	      startPage -= pageBlock;

	      System.out.println("startPage : " + startPage);

	      // 마지막 페이지
	      // 3 = 1 + 3 - 1
	      endPage = startPage + pageBlock - 1;
	      if (endPage > pageCount)
	      endPage = pageCount;

	      System.out.println("endPage : " + endPage);

	      System.out.println("==============================================");

	      ArrayList<LoanHistoryVO> loanPay = null;

	      System.out.println("loan_id => " + loan_id);
	      Map<String, Object> map1 = new HashMap<String, Object>();
	      map1.put("start", start);
	      map1.put("end", end);
	      map1.put("member_id", member_id);
	      map1.put("loan_id", loan_id);

	      // 5-2 게시글 목록 조회
	      System.out.println("djlfsfjosejzejlf===============>" + map1.values().stream().map((value) -> value));
	         loanPay = dao.getLoanPayList(map1);

	      // 6단계. jsp로 전달하기 위해 request나 session에 처리결과를 저장
	      model.addAttribute("loanPay", loanPay); // 게시글 목록
	      model.addAttribute("cnt", cnt); // 게시글 갯수
	      model.addAttribute("pageNum", pageNum); // 페이지 번호
	      model.addAttribute("number", number); // 출력용 글번호

	      if (cnt > 0) {
	      model.addAttribute("startPage", startPage); // 시작페이지
	      model.addAttribute("endPage", endPage); // 마지막페이지
	      model.addAttribute("pageBlock", pageBlock); // 한 블럭당 페이지 갯수
	      model.addAttribute("pageCount", pageCount); // 페이지 갯수
	      model.addAttribute("currentPage", currentPage); // 현재페이지
	      }
	   }

	// 계좌 조회(연동)
	@Override
	public void myAccountList(HttpServletRequest req, Model model) {

		String member_id = (String) req.getSession().getAttribute("customerID");

		List<AccountVO> dtos = dao.getAccountConnected(member_id);

		model.addAttribute("dtos", dtos);

	}

	// 계좌 연동체크(복환)
	@Override
	public void accountConnectCheck(HttpServletRequest req, Model model) {

		String member_id = (String) req.getSession().getAttribute("customerID");
		String unique_key = dao.getUniqueKey(member_id);

		List<AccountVO> dtos = dao.accountConnectCheck(unique_key);

		model.addAttribute("dtos", dtos);

	}

	// 계좌 연동(복환)
	@Override
	public int accountConnectAction(HttpServletRequest req, Model model) {

		String member_id = (String) req.getSession().getAttribute("customerID");
		String unique_key = dao.getUniqueKey(member_id);
		String account_bankCode = req.getParameter("account_bankCode");

		System.out.println("bankcode:" + account_bankCode);

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("member_id", member_id);
		map.put("unique_key", unique_key);
		map.put("account_bankCode", account_bankCode);

		return dao.accountConnectAction(map);
	}

	// 계좌 연동해제(복환)
	@Override
	public int accountDisConnectAction(HttpServletRequest req, Model model) {

		String member_id = (String) req.getSession().getAttribute("customerID");

		String account_bankCode = req.getParameter("account_bankCode");

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("member_id", member_id);
		map.put("account_bankCode", account_bankCode);

		return dao.accountDisConnectAction(map);
	}

	// 계좌 연동관리(복환)
	@Override
	public void accountConnectedList(HttpServletRequest req, Model model) {

		String member_id = (String) req.getSession().getAttribute("customerID");

		List<AccountVO> list = dao.getAccountConnected(member_id);

		model.addAttribute("dtos", list);
	}

	// 은행별 계좌조회(복환)
	@Override
	public ArrayList<AccountVO> getAccountList(HttpServletRequest req, Model model) {

		String member_id = (String) req.getSession().getAttribute("customerID");

		return dao.getAccountList(member_id);

	}

	// 자동이체신청
	public void insertAutoTransfer(HttpServletRequest req, Model model) {
		AutoTransferVO vo = new AutoTransferVO();
		vo.setMember_id(req.getParameter("member_id"));
		vo.setAccount_id(req.getParameter("account_id"));
		vo.setAuto_senderAccount(req.getParameter("auto_senderAccount"));
		vo.setAuto_money(Integer.parseInt(req.getParameter("auto_money")));
		vo.setAuto_outDate(Integer.parseInt(req.getParameter("auto_outDate")));
		vo.setAuto_registDate(Date.valueOf(req.getParameter("auto_registDate")));
		vo.setAuto_expirationDate(Date.valueOf(req.getParameter("auto_expirationDate")));
		vo.setAuto_type(Integer.parseInt(req.getParameter("auto_type")));
		vo.setAuto_inPlace(req.getParameter("auto_inPlace"));
		vo.setAuto_senderAccount_bankCode(Integer.parseInt(req.getParameter("sendAccountBankCode")));

		int insertCnt = 0;
		insertCnt = dao.insertAutoTransfer(vo);
		System.out.println("자동이체 신청 insertCnt : " + insertCnt);
		model.addAttribute("insertCnt", insertCnt);
	}

	// 회원자동이체목록
	public void getMyAutoTransfer(HttpServletRequest req, Model model) {
		String member_id = (String) req.getSession().getAttribute("customerID");

		ArrayList<AutoTransferVO> dtos = new ArrayList<AutoTransferVO>();
		dtos = dao.getMyAutoTransfer(member_id);

		model.addAttribute("dtos", dtos);
	}

	// 회원 자동이체 해지
	public void deleteAutoTransfer(HttpServletRequest req, Model model) {
		int auto_id = Integer.parseInt(req.getParameter("auto_id"));
		int deleteCnt = 0;
		deleteCnt = dao.deleteAutoTransfer(auto_id);
		System.out.println("자동이체 해지 deleteCnt : " + deleteCnt);
		model.addAttribute("deleteCnt", deleteCnt);
	}

	// 자동이체 실행
	@Override
	public void AutoTransferAction() {
		// 오늘날짜(해당일자) 구하기
		SimpleDateFormat format = new SimpleDateFormat("dd");
		java.util.Date date = new java.util.Date();
		String days = format.format(date);

		System.out.println("오늘날짜day 자동이체 실행" + days + "일");

		// 자동이체 조건
		int auto_id = 0;
		String account_id = "";
		String auto_senderAccount = "";
		int auto_money = 0;
		String auto_inPlace = "";
		String member_id = "";
		int sendAccountBankCode = 0;

		// int auto_status=0;
		int day = Integer.parseInt(days);

		// 오늘날짜로 자동이체조회해서 오늘납부할리스트들 가져오기
		ArrayList<AutoTransferVO> transferInfo = dao.selectByDay(day);
		System.out.println("오늘날짜 자동이체 조회 : " + transferInfo.size());
		// 자동이체설정
		if (transferInfo.size() != 0) {
			int account_balance = 0; // 잔액
			for (int i = 0; i < transferInfo.size(); i++) {
				auto_id = transferInfo.get(i).getAuto_id();
				account_id = transferInfo.get(i).getAccount_id(); // 내 계좌번호
				auto_senderAccount = transferInfo.get(i).getAuto_senderAccount(); // 받는사람계좌번호
				auto_money = transferInfo.get(i).getAuto_money();
				auto_inPlace = transferInfo.get(i).getAuto_inPlace();
				member_id = transferInfo.get(i).getMember_id();
				sendAccountBankCode = transferInfo.get(i).getAuto_senderAccount_bankCode();

				// 내 계좌 잔액조회
				account_balance = dao.selectAccountBalance(account_id);

				if (account_balance >= auto_money) {
					// 이체시작
					// 1.이체테이블에 내역 추가
					TransferVO vo = new TransferVO();
					vo.setAccount_id(account_id);
					vo.setTransfer_senderAccount(auto_senderAccount);
					vo.setTransfer_money(auto_money);
					vo.setTransfer_outComment(auto_inPlace);
					vo.setMember_id(member_id);
					vo.setTransfer_bankCode(sendAccountBankCode);
					dao.insertTranferByAuto(vo);

					// 2.자동이체내역테이블에 내역추가
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("auto_id", auto_id);
					map.put("member_id", member_id);
					map.put("auto_money", auto_money);
					map.put("auto_inplace", auto_inPlace);
					dao.insertAutoTransferList(map);

					// 3.납부한거 계좌반영(내 계좌 잔액감소)
					Map<String, Object> map1 = new HashMap<String, Object>();
					map1.put("account_balance", (account_balance - auto_money));
					map1.put("account_id", account_id);
					dao.updateAccountAutoTransfer(map1);

					// 자동이체 테이블에 최신납부내역 갱신
					System.out.println("자동이체 성공");

					// 4.자동이체 테이블에 납부내역 갱신
					dao.updateAutoTransfer(auto_id);
				} else {
					System.out.println("자동이체 실패 : 계좌잔액이 부족합니다.");
					// 자동이체내역리스트에 실패내역 추가
					dao.failAutoTransferList(auto_id);
				}
			}
		} else {
			System.out.println("자동이체할 데이터가 없습니다.");

			ArrayList<LoanHistoryVO> loanPay = null;

		}
	}


	
}
