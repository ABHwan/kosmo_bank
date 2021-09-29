package com.spring.bank.admin.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spring.bank.product.vo.DepositProductVO;
import com.spring.bank.user.vo.CustomerAccountVO;
import com.spring.bank.user.vo.InquiryVO;
import com.spring.bank.user.vo.NoticeVO;
import com.spring.bank.user.vo.UserVO;
import com.spring.bank.user.vo.faqVO;

@Repository
public class AdminDAOImpl implements AdminDAO {
	
	@Autowired
	SqlSession sqlSession;

	// 관리자페이지 회원수 구하기
	@Override
	public int getCustomerCnt() {
		return sqlSession.getMapper(AdminDAO.class).getCustomerCnt();
	}

	// 관리자페이지 회원목록
	@Override
	public ArrayList<UserVO> getCustomerList(Map<String, Object> map) {
		return sqlSession.getMapper(AdminDAO.class).getCustomerList(map);
	}

	// 관리자페이지 회원수(검색어별)
	@Override
	public int getCustomerSearchCnt(String search) {
		return sqlSession.getMapper(AdminDAO.class).getCustomerSearchCnt(search);
	}
	
	// 관리자페이지  회원검색
	@Override
	public ArrayList<UserVO> getCustomerSearchList(Map<String, Object> map) {
		return sqlSession.getMapper(AdminDAO.class).getCustomerSearchList(map);
	}

	// 관리자 페이지 회원 삭제
	@Override
	public int deleteCustomer(String member_id) {
		return sqlSession.getMapper(AdminDAO.class).deleteCustomer(member_id);
	}

	// 관리자 페이지 예금 상품 등록
	@Override
	public int insertDepositProduct(DepositProductVO vo) {
		return sqlSession.getMapper(AdminDAO.class).insertDepositProduct(vo);
	}

	// 관리자 페이지 예금 상품 수
	@Override
	public int getDepositProductCnt() {
		return sqlSession.getMapper(AdminDAO.class).getDepositProductCnt();
	}
	
	// 관리자 페이지 예금 상품 조회
	@Override
	public ArrayList<DepositProductVO> selectDepositProduct(Map<String, Object> map) {
		return sqlSession.getMapper(AdminDAO.class).selectDepositProduct(map);
	}

	// 관리자 페이지 예금 상품 수(검색결과수)
	@Override
	public int getDepositProductSearchCnt(String search) {
		return sqlSession.getMapper(AdminDAO.class).getDepositProductSearchCnt(search);
	}
	
	// 관리자 페이지 예금 상품 검색(입력받아서 검색)
	@Override
	public ArrayList<DepositProductVO> searchDepositProduct(Map<String, Object> map) {
		return sqlSession.getMapper(AdminDAO.class).searchDepositProduct(map);
	}
		
	// 관리자 페이지 예금 상품 수정
	@Override
	public int updateDepositProduct(DepositProductVO vo) {
		return 0;
	}

	// // 관리자 페이지 예금 상품 삭제
	@Override
	public int deleteDepositProduct(String deposit_product_name) {
		return sqlSession.getMapper(AdminDAO.class).deleteDepositProduct(deposit_product_name);
	}
	
	//qna 개수(지현)
	@Override
	public int getInquiryCnt() {
		
		int selectCnt = sqlSession.selectOne("com.spring.bank.admin.dao.AdminDAO.getInquiryCnt");
		return selectCnt;
	}

	//qna 조회(지현)
	@Override
	public List<InquiryVO> getInquiryList(Map<String, Integer> map) {
		// List<InquiryVO> list = sqlSession.selectList("com.spring.bank.admin.dao.AdminDAO.getInquiryList"); // ........................
		List<InquiryVO> list = sqlSession.selectList("com.spring.bank.admin.dao.AdminDAO.getInquiryList", map); // 너 죽인다. 파라미터 안줬자나....
		return list;
	}

	//자주묻는 질문 갯수 (지현)
	@Override
	public int getFaqCnt() {
		int selectCnt = sqlSession.selectOne("com.spring.bank.admin.dao.AdminDAO.getFaqCnt");				;
		
		
		return selectCnt;
	}

	//자주묻는 질문 조회(지현)
	@Override
	public List<faqVO> getFaqList(Map<String, Integer> map) {
		
		List<faqVO> list = sqlSession.selectList("com.spring.bank.admin.dao.AdminDAO.getFaqList", map);
		return list;
	}

	//자주묻는 질문  등록하기(지현)
	@Override
	public int faqAdd(faqVO vo) {
		int insertCnt = sqlSession.insert("com.spring.bank.admin.dao.AdminDAO.faqAdd", vo);
				
		return insertCnt;
	}

	//faq 수정 상세 조회(지현)
	@Override
	public faqVO getFaqDetail(int faq_id) {
		faqVO vo = sqlSession.selectOne("com.spring.bank.admin.dao.AdminDAO.getFaqDetail", faq_id);
		
		return vo;
	}

	//faq 수정 처리(지현)
	@Override
	public int updateFaq(faqVO vo) {
		int updateCnt = sqlSession.update("com.spring.bank.admin.dao.AdminDAO.updateFaq", vo);
		
		return updateCnt;
	}
	
	
	//faq삭제처리(지현)
	@Override
	public int deleteFaq(int faq_id) {
		int deleteCnt = sqlSession.delete("com.spring.bank.admin.dao.AdminDAO.deleteFaq", faq_id);
		
		return deleteCnt;
	}
	
	// 관리자 페이지 회원계좌수
	public int getCustomerAccountCnt() {
		return sqlSession.getMapper(AdminDAO.class).getCustomerAccountCnt();
	}
	
	// 관리자 페이지 회원계좌목록
	public ArrayList<CustomerAccountVO> getCustomerAccountList(Map<String, Object> map){
		return sqlSession.getMapper(AdminDAO.class).getCustomerAccountList(map);
	}
	
	// 관리자 페이지 회원계좌 검색결과 수
	public int getSerachCustomerAccountCnt(String search) {
		return sqlSession.getMapper(AdminDAO.class).getSerachCustomerAccountCnt(search);
	}
	
	// 관리자 페이지 회원계좌 검색결과목록
	public ArrayList<CustomerAccountVO> getSearchCustomerAccountList(Map<String, Object> map){
		return sqlSession.getMapper(AdminDAO.class).getSearchCustomerAccountList(map);
	}
	
	// 공지사항 쓰기 처리(민재)
	@Override
	public int mngNoticeWriteAction(NoticeVO vo) {
		
		return sqlSession.insert("com.spring.bank.admin.dao.AdminDAO.mngNoticeWriteAction", vo);
	}
	
	// 공지사항 - 비밀번호 인증(민재)
	@Override
	public int noticePWDCheck(Map<String, Object> map) {
		
		return sqlSession.selectOne("com.spring.bank.admin.dao.AdminDAO.noticePWDCheck", map);
	}
	
	// 공지사항 상세페이지(민재)
	@Override
	public NoticeVO getNoticeDetail(int notice_num) {
		
		return sqlSession.selectOne("com.spring.bank.admin.dao.AdminDAO.getNoticeDetail", notice_num);
	}
	
	// 공지사항 수정처리(민재)
	@Override
	public int noticeModifyAction(NoticeVO vo) {
		
		return sqlSession.update("com.spring.bank.admin.dao.AdminDAO.noticeModifyAction", vo);
	}
	
	// 공지사항 수정처리(민재)
	@Override
	public int noticeDeleteAction(int notice_num) {
		
		return sqlSession.delete("com.spring.bank.admin.dao.AdminDAO.noticeDeleteAction", notice_num);
	}
}
