<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/setting.jsp" %>
<%@ include file="/WEB-INF/views/include/bootstrap.jsp" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>코스모 뱅크</title>
</head>
<body>	
	<div class="wrapper">
		<jsp:include page="/WEB-INF/views/include/header.jsp" />
		<jsp:include page="/WEB-INF/views/include/mngSidebar.jsp" />
		<!-- 메인 폼-->
		<div class="main-panel">
			<div class="content">
				<!-- 고정헤더 -->
				<div class="panel-header bg-primary-gradient" style="height: 300px;">
					<div class="page-inner py-5">
						<div class="d-flex align-items-left align-items-md-center flex-column flex-md-row">
							<div>
								<h1 class="text-white pb-2 fw-bold">KOSMO BANK</h1> <br/>
								<h2 class="text-white op-7 mb-2">KOSMO BANK에 오신 것을 환영합니다.<br/>
									저희는 고객님의 <strong>자산관리</strong>를 효율적이고, 안전하게 도와드립니다. <br/>
									또한 <strong>오픈뱅킹</strong> 서비스를 활용하여 보다 편리하게 통합하여 금융상품을 이용하실 수 있습니다.</h2>
							</div>
						</div>
					</div>
				</div>
				<!-- 고정헤더 -->
				<div class="page-inner">
					<h4 class="page-title">대출상품 관리</h4>
					<div class="row">
						<div class="col">
							<div class="card">
								<div class="card-header">
									<div class="card-title">대출상품 등록</div>
								</div>
								<div class="card-body">
									<form action="loanProductUpdateAction.do" name="loanUpdateform" method="post">
					 					<sec:csrfInput />
										<input type="hidden" id="pre_loan_product_name" name="pre_loan_product_name" value="${loanProduct.loan_product_name}"/> 
										<table class="table table-hover card-table">
											<tr>
												<th><label for="loan_product_name">대출 상품명</label></th>
												<td><input type="text" id="loan_product_name" name="loan_product_name" value="${loanProduct.loan_product_name}" class="form-control input-border-bottom" autofocus required></td>
											</tr>
											<tr>
												<th><label for="loan_product_rate">대출 금리(%)</label></th>
												<td><input type="text" id="loan_product_rate" name="loan_product_rate" value="${loanProduct.loan_product_rate}" class="form-control input-border-bottom" placeholder="예) 5.0" required></td>
											</tr>
											<tr>
												<th><label for="loan_product_minPrice">최소 대출 금액(원)</label></th>
												<td><input type="text" id="loan_product_minPrice" name="loan_product_minPrice" value="${loanProduct.loan_product_minPrice}" class="form-control input-border-bottom" placeholder="예) 500000000" required></td>
											</tr>
											<tr>
												<th><label for="loan_product_maxPrice">최대 대출 금액(원)</label></th>
												<td><input type="text" id="loan_product_maxPrice" name="loan_product_maxPrice" value="${loanProduct.loan_product_maxPrice}" class="form-control input-border-bottom" placeholder="예) 5000000000" required></td>
											</tr>
											<tr>
												<th><label for="loan_product_minDate">최소 대출 기간(년)</label></th>
												<td><input type="text" id="loan_product_minDate" name="loan_product_minDate" value="${loanProduct.loan_product_minDate}" class="form-control input-border-bottom" placeholder="예) 1" required></td>
											</tr>
											<tr>
												<th><label for="loan_product_maxDate">최대 대출 기간(년)</label></th>
												<td><input type="text" id="loan_product_maxDate" name="loan_product_maxDate" value="${loanProduct.loan_product_maxDate}" class="form-control input-border-bottom" placeholder="예) 10" required></td>
											</tr>
											<tr>
												<th><label for="loan_product_bankCode">은행</label></th>
												<td><select class="form-control form-control" name="loan_product_bankCode">
														<c:forEach var="i" begin="0" end="6">
															<option value="${i}"
																<c:if test="${loanProduct.loan_product_bankCode == i}">
																	selected
																</c:if> >
																<c:choose>
																	<c:when test="${i == 0}">
																		미기재
																	</c:when>
																	<c:when test="${i == 1}">
																		국민
																	</c:when>
																	<c:when test="${i == 2}">
																		우리
																	</c:when>
																	<c:when test="${i == 3}">
																		농협
																	</c:when>
																	<c:when test="${i == 4}">
																		신한
																	</c:when>
																	<c:when test="${i == 5}">
																		하나
																	</c:when>
																	<c:when test="${i == 6}">
																		코스모
																	</c:when>
																</c:choose>
															</option>
														</c:forEach>
												</select>기존 : 	<c:choose>												
																	<c:when test="${loanProduct.loan_product_bankCode == 0}">
																		미기재
																	</c:when>
																	<c:when test="${loanProduct.loan_product_bankCode == 1}">
																		국민
																	</c:when>
																	<c:when test="${loanProduct.loan_product_bankCode == 2}">
																		우리
																	</c:when>
																	<c:when test="${loanProduct.loan_product_bankCode == 3}">
																		농협
																	</c:when>
																	<c:when test="${loanProduct.loan_product_bankCode == 4}">
																		신한
																	</c:when>
																	<c:when test="${loanProduct.loan_product_bankCode == 5}">
																		하나
																	</c:when>
																	<c:when test="${loanProduct.loan_product_bankCode == 6}">
																		코스모
																	</c:when>
																</c:choose></td>
											</tr>
											<tr>
												<th><label for="loan_product_prepaymentRate">중도상환수수료 요율(%)</label></th>
												<td><input type="text" id="loan_product_prepaymentRate" name="loan_product_prepaymentRate" value="${loanProduct.loan_product_prepaymentRate}" class="form-control input-border-bottom" placeholder="예) 5.0" required></td>
											</tr>
											<tr>
												<th><label for="loan_product_summary">대출 상품 설명</label></th>
												<td><textarea id="loan_product_summary" name="loan_product_summary" class="form-control input-border-bottom" placeholder="상세 설명 (2000자 제한)">${loanProduct.loan_product_summary}</textarea></td>
											</tr>
										</table>
										<div align="center"><br><br>
											<input type="submit" class="btn btn-primary" value="완료">&emsp;
											<input type="reset"	class="btn btn-primary" value="초기화">
										</div>
									</form>
								</div>
							</div>
						</div>
					</div>	
					</div>	
				</div>
			</div>
		</div>
		 
		<jsp:include page="/WEB-INF/views/include/footer.jsp" /> 
 
</body>
</html>