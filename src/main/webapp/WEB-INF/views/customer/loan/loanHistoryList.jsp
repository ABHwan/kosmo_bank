<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/setting.jsp"%>
<%@ include file="/WEB-INF/views/include/bootstrap.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>코스모 뱅크</title>
</head>
<body>
	<div class="wrapper">
		<jsp:include page="/WEB-INF/views/include/header.jsp" />
		<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />
		<!-- 메인 폼-->
		<div class="main-panel">
			<div class="content">
				<!-- 고정헤더 -->
				<div class="panel-header bg-primary-gradient" style="height: 300px;">
					<div class="page-inner py-5">
						<div
							class="d-flex align-items-left align-items-md-center flex-column flex-md-row">
							<div>
								<h1 class="text-white pb-2 fw-bold">KOSMO BANK</h1>
								<br />
								<h2 class="text-white op-7 mb-2">
									KOSMO BANK에 오신 것을 환영합니다.<br /> 저희는 고객님의 <strong>자산관리</strong>를
									효율적이고, 안전하게 도와드립니다. </br> 또한 <strong>오픈뱅킹</strong> 서비스를 활용하여 보다 편리하게
									통합하여 금융상품을 이용하실 수 있습니다.
								</h2>
							</div>
						</div>
					</div>
				</div>
				<!-- 고정헤더 -->
				<div class="page-inner">
					<div class="row">
						<div class="col">
							<div class="card">
								<div class="card-header">
									<div class="card-title">
										<div class="d-flex justify-content-between mt-2">
											대출 상환 내역 조회
											<%-- <form action="searchLoanList.do" method="post" name="searchform">
												<sec:csrfInput />
												<input type="search" class="form-control form-control-sm" placeholder="내역검색" name="keyword">
												<button class="form-control btn btn-link">
													<i class="fas fa-search"></i>
												</button>
											</form> --%>
										</div>
									</div>
								</div>
								<div class="card-body"> 
									<form action="loanList" method="post"
										id="loanform">
										<sec:csrfInput />
										<table class="table table-hover card-table">
											<thead>
												<tr>
													<th scope="col">대출상환번호</th>
													<th scope="col">대출번호(조인해서 정보끌어와야함 ㅅㅈㅈ)</th>
													<th scope="col">이체번호</th>
													<th scope="col">대출상환일자</th>
													<th scope="col">대출상환종류</th>
													<th scope="col">대출상환금액</th>
												</tr>
											</thead>
											<tbody>
												<c:forEach var="l" items="${loanHistorys}">
													<tr>
														<td>${l.loan_history_id}</td>
														<td>${l.loan_id}</td>
														<td>${l.transfer_id}</td>
														<td>${l.loan_history_date}</td>
														<td>${l.loan_history_state}</td>
														<td>${l.loan_history_amount}</td>
													</tr>
												</c:forEach>
												<c:if test="${cnt == 0}">
													<tr>
														<td colspan="10">대출 상환 내역이 존재하지 않습니다.</td>
													</tr>
												</c:if>
											</tbody>
										</table>
									</form>
									<!-- paging -->
									<div class="d-flex justify-content-center">
										<ul class="pagination pg-primary mt-5">
											<c:if test="${cnt > 0}">
												<!-- 이전블록[«] -->
												<c:if test="${startPage > pageBlock}">
													<li class="page-item"><a class="page-link"
														href="loanList.do?pageNum=${startPage - pageBlock}"
														aria-label="Previous"> <span aria-hidden="true">«</span>
															<span class="sr-only">Previous</span>
													</a></li>
												</c:if>

												<!-- 블록 내의 페이지 번호 -->
												<c:forEach var="i" begin="${startPage}" end="${endPage}">
													<c:if test="${i == currentPage}">
														<li class="page-item active"><a class="page-link"
															href="loanList.do?pageNum=${i}">${i}</a></li>
													</c:if>

													<c:if test="${i != currentPage}">
														<li class="page-item"><a class="page-link"
															href="loanList.do?pageNum=${i}">${i}</a></li>
													</c:if>
												</c:forEach>

												<!-- 다음블록[»] -->
												<c:if test="${pageCount > endPage}">
													<li class="page-item"><a class="page-link"
														href="loanList.do?pageNum=${startPage + pageBlock}"
														aria-label="Next"> <span aria-hidden="true">»</span> <span
															class="sr-only">Next</span>
													</a></li>
												</c:if>
											</c:if>
										</ul>
										<div align="right"></div>
									</div>
									<!-- paging -->
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<jsp:include page="/WEB-INF/views/include/footer.jsp" />

	<!--   Core JS Files   -->
	<script src="${rePath}js/core/jquery.3.2.1.min.js"></script>
	<script src="${rePath}js/core/popper.min.js"></script>
	<script src="${rePath}js/core/bootstrap.min.js"></script>

	<!-- jQuery UI -->
	<script
		src="${rePath}js/plugin/jquery-ui-1.12.1.custom/jquery-ui.min.js"></script>
	<script
		src="${rePath}js/plugin/jquery-ui-touch-punch/jquery.ui.touch-punch.min.js"></script>

	<!-- jQuery Scrollbar -->
	<script
		src="${rePath}js/plugin/jquery-scrollbar/jquery.scrollbar.min.js"></script>


	<!-- Chart JS -->
	<script src="${rePath}js/plugin/chart.js/chart.min.js"></script>

	<!-- jQuery Sparkline -->
	<script
		src="${rePath}js/plugin/jquery.sparkline/jquery.sparkline.min.js"></script>

	<!-- Chart Circle -->
	<script src="${rePath}js/plugin/chart-circle/circles.min.js"></script>

	<!-- Datatables -->
	<script src="${rePath}js/plugin/datatables/datatables.min.js"></script>

	<!-- Bootstrap Notify -->
	<script
		src="${rePath}js/plugin/bootstrap-notify/bootstrap-notify.min.js"></script>

	<!-- jQuery Vector Maps -->
	<script src="${rePath}js/plugin/jqvmap/jquery.vmap.min.js"></script>
	<script src="${rePath}js/plugin/jqvmap/maps/jquery.vmap.world.js"></script>

	<!-- Sweet Alert -->
	<script src="${rePath}js/plugin/sweetalert/sweetalert.min.js"></script>

	<!-- Atlantis JS -->
	<script src="${rePath}js/atlantis.min.js"></script>

	<!-- Atlantis DEMO methods, don't include it in your project! -->
	<script src="${rePath}js/setting-demo.js"></script>
	<script src="${rePath}js/demo.js"></script>
</body>
</html>