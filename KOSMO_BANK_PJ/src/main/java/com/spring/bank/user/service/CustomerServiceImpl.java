package com.spring.bank.user.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.servlet.http.HttpServletRequest;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.spring.bank.customer.encrypt.UserAuthenticationService;
import com.spring.bank.user.dao.CustomerDAOImpl;
import com.spring.bank.user.vo.UserVO;


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
	
	// 아이디 중복확인
	@Override
	public int confirmIdAction(Map<String, Object> map) {
		
		System.out.println("[서비스 => ID 중복확인 처리]");
		
		System.out.println(map.get("member_id"));
		
		return dao.idCheck(map);
	}

	// 회원가입 처리
	@Override
	public void registerAction(HttpServletRequest req, Model model) {
		System.out.println("[서비스 => 회원가입 처리]");
		
		String access_token = req.getParameter("access_token");
		String refresh_token = req.getParameter("refresh_token");
		String user_seq_no = req.getParameter("user_seq_no");
		
		System.out.println("at : " + access_token);
		System.out.println("rt : " + refresh_token);
		System.out.println("usn : " + user_seq_no);
		
		
		// 3단계. 화면으로부터 입력 받은 값을 받아온다. 바구니에 담는다.
		UserVO vo = new UserVO();
		
		// String strID = req.getParameter("id");
		/*
		CREATE TABLE members(
		    id      VARCHAR2(20)    PRIMARY KEY,
		    password     VARCHAR2(20)    NOT NULL,                                              
		    name    VARCHAR2(20)     NOT NULL,
		    birth   DATE,
		    hp     NUMBER(11)      NOT NULL UNIQUE,
		    email   VARCHAR2(50)    UNIQUE,
		    zipcode NUMBER(5)       NOT NULL,
		    addr1 VARCHAR2(50)    NOT NULL,
		    addr2 VARCHAR2(50),
		    addr3 VARCHAR2(50),
		    indate    DATE          DEFAULT SYSDATE
		);
		*/
		String strPassword = bCryptPasswordEncoder.encode(req.getParameter("password"));
		
		String hp = "";
		String hp1 = req.getParameter("hp1");
		String hp2 = req.getParameter("hp2");
		String hp3 = req.getParameter("hp3");
		
		// hp가 필수가 아니므로 null 값이 들어올 수 있으므로 값이 존재할 때만 처리
		if(!hp1.equals("") && !hp2.equals("") && !hp3.equals("")) {
			hp = hp1 + "-" + hp2 + "-" + hp3;
		}
		
		String email = "";
		String email1 = req.getParameter("email1");
		String email2 = req.getParameter("email2");
		
		email = email1 + "@" + email2;
		
		String zipcode= req.getParameter("address_zipcode");
		/*
		 * if(zipcode.length() == 4 ) { zipcode = "0" + zipcode; }
		 */
		
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
		vo.setAccess_token(req.getParameter("access_token"));
		vo.setRefresh_token(req.getParameter("refresh_token"));
		vo.setUser_seq_no(req.getParameter("user_seq_no"));
		
		// regDate는 입력값이 없으면 defalut가 sysdate
		vo.setMember_indate(new Timestamp(System.currentTimeMillis()));
		
		// CustomerDAOImpl dao = new CustomerDAOImpl();
		// 4단계. 싱글톤 방식으로 dao 객체 생성
		
		// 5단계. 회원가입 처리
		int insertCnt = dao.insertUser(vo);
		System.out.println("insertCnt : " + insertCnt);
		
		// 6단계. jsp로 결과 전달(request나 session으로 처리 결과를 저장 후)
		req.setAttribute("insertCnt", insertCnt);
		model.addAttribute("selectCnt", insertCnt);
		// 이메일 인증
		req.setAttribute("email", email);
	}
	
	// 이메일 인증 성공 처리
//	@Override
//	public void emailSuccess(HttpServletRequest req, Model model) {
//		String email = req.getParameter("email");
//		
//		UserVO vo = new UserVO();
//		
//		vo.setEmail(email);
//		vo.setKey(1);
//		
//		dao.emailSuccess(vo);
//		
//	}

	@Override
	public void deleteCustomerAction(HttpServletRequest req, Model model) {
		System.out.println("[서비스 => 회원탈퇴 처리]");
		
		// 3단계. 화면으로부터 입력 받은 값을 가져오기
		String id = (String) req.getSession().getAttribute("userID");
		
		// 4단계. 싱글톤 방식으로 dao 객체 생성, 다형성 적용
		// 5-1단계. 회원탈퇴 인증 처리
		int deleteCnt = 0;
		
		// 5-2단계. 인증성공 시 탈퇴처리
		if(id != null) {
			deleteCnt = dao.deleteUser(id);
			System.out.println("deleteCnt : " + deleteCnt);
		}
		
		// 6단계. jsp로 결과 전달(request나 session으로 처리 결과를 저장 후)
		req.setAttribute("deleteCnt", deleteCnt);
	}

	@Override
	public void modifyDetailAction(HttpServletRequest req, Model model) {
		
		System.out.println("[서비스 => 회원수정 인증 및 상세화면]");
		
		String id = (String) req.getSession().getAttribute("userID");
		
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
		if(chk) {
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
		String strId = (String) req.getSession().getAttribute("userID");
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
		if(!hp1.equals("") && !hp2.equals("") && !hp3.equals("")) {
			hp = hp1 + "-" + hp2 + "-" + hp3;
		}
		
		String email1 = req.getParameter("email1");
		String email2 = req.getParameter("email2");
		String email = email1 + "@" + email2;
		
		
		String password = "";
		String passwordChange = req.getParameter("password_change");
		String enPasswordChange= bCryptPasswordEncoder.encode(passwordChange);
		
		
		if(idPwdCheck == 1) {
			// 비밀번호 변경 값이 존재하지 않을 때
			if(passwordChange == "") {
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
		vo.setMember_id((String)req.getSession().getAttribute("userID"));
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
		String strId = (String) req.getSession().getAttribute("userID");
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
		if(vo != null) {
			System.out.println("찾은 id : " + vo.getMember_id());
		}
		
		// jsp로 결과 전달
		model.addAttribute("vo", vo);
	}

	// 임시비밀번호로 변경하고 이메일 전송
	@Override
	public void sendEmail(Map<String, Object> map) {
		try{
			
            MimeMessage message = mailSender.createMimeMessage();
            String txt = "KOSMO BANK 임시비밀번호 전송메일입니다. <br/>" 
            		+ "임시비밀번호 : " + (String)map.get("member_password")
            		+ "<br/> 해당 비밀번호로 로그인 하시고 비밀번호 변경해주세요~!";
            message.setSubject("KOSMO BANK 임시비밀번호 전송메일입니다");
            message.setText(txt, "UTF-8", "html");
            message.setFrom(new InternetAddress("xkrrhsdl7@gmail.com")); // 보내는사람
            message.addRecipient(RecipientType.TO, new InternetAddress((String)map.get("member_email"))); // 받는사람
            mailSender.send(message);

      }catch(Exception e){
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
				
		if(vo == null) {
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

}
