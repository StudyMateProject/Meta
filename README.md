# Meta

## 메타버스 구현중

### 📌 01/26
#### ✔ 메인 페이지 대략적인 구현
#### ✔ 로그인 및 회원가입 간단하게 연동
#### ✔ 메타 방 생성 및 입장 구현
#### ✔ 메타 방 내부 캐릭터 이동 구현
#### ✔ 웹소켓(STOMP)을 이용한 각 메타 방 내부 채팅 구현
현재까지 대강 구현해둔 로그인과 소켓부분을 메인 페이지를 하나 만들어서 연동하고 본격적으로 메타쪽을 구현해보도록 할것이다.

##### ~~사용된 데이터베이스 : MySQL - soju~~ (아래 수정된 테이블 사용)
	CREATE DATABASE soju;
	USE soju;

##### ~~사용된 테이블 : Member, MetaRoom~~ (아래 수정된 테이블 사용)
	멤버
	CREATE TABLE Member (
		emailId VARCHAR(50) PRIMARY KEY, #이메일 형식 아이디
		pwd VARCHAR(255) NOT NULL, #비밀번호
		name VARCHAR(10) NOT NULL, #이름
		nickname VARCHAR(20) UNIQUE NOT NULL, #닉네임
		birthday DATE NOT NULL, #생년월일
		gender VARCHAR(1) NOT NULL, #성별
		phoneNumber VARCHAR(15) UNIQUE NOT NULL, #핸드폰 번호
		address VARCHAR(100) NOT NULL, #주소
		studyType VARCHAR(10) NOT NULL, #관심있는 분야
		platform VARCHAR(10) NOT NULL, #플랫폼
		roleName VARCHAR(100) NOT NULL #Spring Security 권한
	);
	
	메타버스 방
	CREATE TABLE MetaRoom (
		metaIdx INT PRIMARY KEY AUTO_INCREMENT, #방 번호
		metaTitle VARCHAR(50) NOT NULL, #방 제목
		metaType VARCHAR(10) NOT NULL, #방 분야
		metaPersonnel INT NOT NULL, #방 모집인원
		metaRecruitingPersonnel INT NOT NULL #방 참여중인 인원
	);

#

### 📌 01/27
#### ✔ 메타 방 내부 참가자 표시 구현
##### 로그인한 유저가 입장하면 현재 참가하고있는 유저들을 참가자 구역에 이름 및 프로필 사진을 작성하여 보여주도록 만들었다.
##### 하지만 이미 입장해있는 유저한테는 새로고침 하기 전까지는 새로 들어온 유저가 참가자 구역에 보이지 않는 문제가 있었다.
##### 그래서 이 문제를 해결하고자 웹소켓(STOMP)을 이용하여 입장메세지를 가져올때 참가자도 같이 가져오도록 하여 실시간으로 입장한 유저들이 참가자 구역에 바로바로 작성되도록 만들었다.
##### 현재 테스트 해본 결과 실시간으로 참가자 닉네임과 사진 이름까진 잘 나오고, 다음에는 사진 이름이 아닌 사진이 나오게 만들어 보도록 하겠다.
#### ✔ 테이블 변경
##### 메타 방 내부에 참가자를 작성하기 위해 테이블을 하나 새로 만드는데 이 테이블 이름을 MetaRoom으로 하는것이 적절해보여 MetaRoom테이블을 Meta로 바꿔준뒤, MetaRoom테이블을 새로 만들었다.
##### 그리고 Member테이블에 프로필 사진을 추가해주었다.

##### ~~사용된 데이터베이스 : MySQL - soju~~ (아래 수정된 테이블 사용)
	CREATE DATABASE soju;
	USE soju;

##### ~~사용된 테이블 : Member, Meta, MetaRoom~~ (아래 수정된 테이블 사용)
	멤버
	CREATE TABLE Member (
		##################회원가입 전 입력##################
		emailId VARCHAR(50) PRIMARY KEY, #이메일 형식 아이디
		pwd VARCHAR(255) NOT NULL, #비밀번호
		name VARCHAR(10) NOT NULL, #이름
		nickname VARCHAR(20) UNIQUE NOT NULL, #닉네임
		birthday DATE NOT NULL, #생년월일
		gender VARCHAR(1) NOT NULL, #성별
		phoneNumber VARCHAR(15) UNIQUE NOT NULL, #핸드폰 번호
		address VARCHAR(100) NOT NULL, #주소
		studyType VARCHAR(10) NOT NULL, #관심있는 분야
		platform VARCHAR(10) NOT NULL, #플랫폼
		roleName VARCHAR(100) NOT NULL, #Spring Security 권한	
		##################회원가입 후 입력##################
		profileImage VARCHAR(100) #프로필 사진
	);
	
	메타버스 방
	CREATE TABLE Meta(
		metaIdx INT PRIMARY KEY AUTO_INCREMENT, #방 번호
		metaTitle VARCHAR(50) NOT NULL, #방 제목
		metaType VARCHAR(10) NOT NULL, #방 분야
		metaPersonnel INT NOT NULL, #방 모집인원
		metaRecruitingPersonnel INT NOT NULL #방 참여중인 인원
	);

	#메타버스 방 내부
	CREATE TABLE MetaRoom(
		idx INT PRIMARY KEY AUTO_INCREMENT, #순서 번호 - 기본키, 시퀀스
		metaNickname VARCHAR(20) UNIQUE NOT NULL, #닉네임
		CONSTRAINT fk_metaRoomNickname FOREIGN KEY(metaNickname) REFERENCES Member(nickname) ON DELETE CASCADE ON UPDATE CASCADE #포린키 연결
		metaProfileImage VARCHAR(100), #프로필 이미지
		metaIdx INT NOT NULL, #방 번호
		CONSTRAINT fk_metaRoomIdx FOREIGN KEY(metaIdx) REFERENCES Meta(metaIdx) ON DELETE CASCADE ON UPDATE CASCADE #포린키 연결
	);

#
	
### 📌 01/29
#### ✔ 메타 방 내부 참가자 표시 구현 2
##### 참가지 구역에 이제 프로필 사진도 같이 가져와서 프로필 사진과 닉네임이 같이 보일 수 있도록 만들었다.
##### 그리고 웹소켓(STOMP)을 사용하여 퇴장하면 참가자 구역에 있는 본인도 같이 실시간으로 삭제되게 만들었다.

#

### 📌 01/30
#### ✔ DTO 생성 및 변경
##### Entity를 사용하여 테스트한 코드들을 각 사용처에 맞게 DTO를 만들어 바꿔주었다.
#### ✔ 중복접속 차단
##### 방 접속 후 새로고침시 중복접속으로 방에 참가자 수가 계속 올라가 실질적인 참가자는 한명인데 방에 참여중인 인원은 꽉차는 문제가 발생했다.
##### 그래서 이 문제를 해결하고자 해당 참가자가 새로 방에 들어온 참가자인지 이미 방에 참여중인 참가자인지를 확인하는 코드를 넣어주었다.
##### 그러면 이제 방에 접속 후 새로고침을 아무리 해도 더이상 참여중인 인원은 증가하지 않는다.

#

### 📌 01/31
#### ✔ 참가 및 퇴장에 따른 참여중인 인원 실시간 변경 및 작성
##### 메타 방 내부 좌측 상단에는 방 정보가 입력되는데 여기서 참여중인 인원은 해당 방에 유저가 참가하고 퇴장하는것에 따른 숫자가 변해야한다.
##### 하지만 이는 유저가 처음 들어올때만 작성될뿐 다시 새로고침 하기 전까지는 처음 들어온 그대로 남아있는 문제가 있었다.
##### 그래서 이 문제를 해결하기 위해 유저가 참가하거나 퇴장할때마다 변경되는 참여중인 인원을 웹소켓(STOMP)에 전달하고 다시 전달받아 실시간으로 참여중인 인원을 변경된 인원으로 수정 작성되도록 만들었다.

#

### 📌 02/01
#### ✔ 검색 종류 및 방 분야에 따라 각기 다른 검색 구현
##### 내가 원하는 메타 방을 찾고싶을 경우 사용하는 방 검색기능을 구현하는데 여기서 검색에는 두가지 구분점이 존재한다.
##### 그건 바로 검색 종류와 방 분야로 검색 종류는 방을 번호로 검색할지 이름으로 검색할지를 결정하고, 방 분야는 어떤 방을 검색할지를 결정한다.
##### 그리고 검색 종류는 종류에 따라 검색어가 다르게 들어가야 하기에 DTO에서 if문을 통해 검색 타입을 각각 다르게 지정되게 만들었다.

#

### 📌 02/03
#### ✔ 참가자가 새로고침 빠르게 연타할 경우 참가자 구역에 중복으로 참가자가 쌓이던 문제 해결
##### 이전에 방에 참가하거나 퇴장하면 참가자 구역에 참가자가 작성되고 삭제되게 만들었었는데, 여기서 새로고침을 빠르게 연타할 경우 참가자 구역에 참가자가 제대로 삭제되지 못한채 참가자가 중복으로 쌓이는 문제가 발생했다.
##### 그래서 이 문제를 해결하기 위해 참가자가 삭제되는 코드를 참가자가 작성되는곳 가장 윗상단에 넣어 if문을 사용하여 이전에 작성되있던 참가자 코드가 삭제되지 못하고 남아있을 경우 먼저 참가자 코드를 삭제하고 다시 참가자 코드를 작성하도록 만들었다.
##### 이러면 이제 더이상 참가자가 아무리 새로고침을 하더라도 매번 삭제 후 작성을 하기에 참가자가 중복으로 쌓이는 문제가 발생하지 않는다.

#

### 📌 02/06
#### ✔ 도배를 막기위해 채팅을 1초안에 5개 이상을 전송할 경우 5초간 채팅을 할 수 없도록 구현
##### 시작시간을 나타내는 start와 메세지 작성시간을 나타내는 end라는 변수를 각각 만들어 Date함수에 now를 사용하여 현재까지 경과된 초를 작성한다.
##### 그러고 메세지 전송 시간에서 시작시간을 빼서 메세지를 전송하는데 얼마가 걸렸는지를 체크한다.
##### 여기서 차이 값이 1000이면 1초를 뜻하는데 1000이 되기전 메세지를 5개이상 보낼 경우 3초간 메세지를 보낼 수 없게 만든다.
##### 그럼 이제 메세지를 연타하며 도배를 할 수 없게 된다.
#### ✔ 엔터키를 클릭시 메세지가 전송되도록 구현
##### 지금까지는 메세지를 전송할때 반드시 버튼을 직접 눌러야 전송이 되는 번거로움이 있었다.
##### 그래서 키보드에 엔터만 클릭해도 메세지가 전송되도록 하기위해 addEventListener에 keypress를 사용하여 event.key로 Enter가 눌렸을때 메세지 전송 메소드가 실행되도록 만들었다.
##### 그럼 이제 더이상 메세지를 전송하기위해 직접 버튼을 클릭하러 갈 필요없이 간단하게 엔터키만 클릭해서 메세지를 전송할 수 있다.

#

### 📌 02/15
#### ✔ 메타 메인 페이지에 로그인 유저 정보를 가져와 프로필 구역에 작성
##### 메타 메인 페에지에 로그인 유저의 정보를 볼 수 있는 프로필 구역을 만들었다.
##### 여기에는 로그인 유저의 프로필 사진, 이름, 관심 있는 분야, 이용권 남은 일수가 작성된다.
#### ✔ 메인 페이지와 메타 메인 페이지에 css을 추가해 꾸미기
##### css를 추가하면서 방 타입별로 구역이 나뉘게 되는데 이러면 원래 사용하던 th:each를 각 방 타입 구역마다 사용해야한다.
##### 하지만 원래 사용하던 th:each는 방 타입 구분없이 한번에 다 가져와 내부에서 th:if를 통해 내부에서 타입별로 나누어주었는데 이 방식을 사용하지 못하게 된다.
##### 혹시나 하나의 객체를 각 구역에서 중복으로 사용할 수 있나 하고 한번 사용해봤으나 역시나 위에 구역에서 사용하면 아랫구역에서는 사용이 안되었다.
##### 그래서 이 문제를 해결하기위해 Controller에서 받아온 생성된 방 List를 foreach문을 사용하여 방 타입별로 한버더 나누어서 각 방 타입마다 List를 생성해서 바인딩하였다.
##### 그럼 이제 방 타입 구역에 맞게 바인딩한 List를 가져다가 사용하면 정상적으로 각 구역별로 방들이 잘 나오는걸 볼 수 있다.

#

### 📌 02/16
#### ✔ 자습실에 채팅 및 참가자 구역 삭제, 체크리스트 및 D-DAY 추가, 알람 기능 및 시간 기능 추가
##### 자습실은 얘기하고 노는공간이 아닌 개인적인 공부를 하는곳이기에 불필요한 채팅이나 참가자 구역을 삭제하였다.
##### 대신 그 자리에 다른 개인적으로 사용할 수 있는것들을 추가하였다.
##### 먼저 삭제된 채팅 구역에는 자신이 오늘 하루 무엇을 할지 작성한 체크리스트와 자신이 목표를 세워둔 D-DAY를 추가하였다.
##### 그 다음 삭제된 참가자 구역에는 현재 날짜 및 시간을 알 수있는 날짜 시간 기능과 공부를 얼마나 했는지 경과된 시간 체크를 도와주는 스톱워치와 자신이 계획한 시간대로 공부할 수 있게 체크를 도와주는 타이머 기능을 추가하였다.
#### ✔ 캔버스 내부 벽 좌표 오류 및 벽이 뚫리던 문제 해결
##### 지금까지 캔버스 내부에 벽의 좌표가 이상하게 설정되거나 캐릭터가 돌아다닐때 키를 두개이상 같이 누를경우 간간히 벽이 뚫리는 현상이 발생했었다.
##### 먼저 벽의 좌표부터 .clientWidth를 사용하여 바로 잡히도록 하였고, 벽이 뚫리던 현상은 눌리는 키의 방향에 벽만 막었던것을 같이 눌리는 키의 방향에 벽까지 막음으로써 이제 키를 두개 이상 눌러도 벽이 뚫리니 않는다.
#### ✔ 자습실 페이지에 css를 추가해 꾸미기
##### 자습실은 기존의 스터디룸이나 카페같이 채팅이 있는 방과는 많이 달라졌기에, 달라진 자습실에 어울리게 새로 css를 추가하여 만들었다.
#

### 📌 02/17
#### ✔ 구글, 네이버 로그인 연동 및 OAuth 2.0 연동
#### ✔ 회원가입 페이지에 SMTP를 사용하여 이메일 인증 번호 전송 아이디 중복 체크 구현, PASS API를 사용하여 휴대폰 인증 구현

#

### 📌 02/20
#### ✔ 방을 나갈 경우 방 내부 참여자 명단에서 제대로 삭제가 안되던 문제 해결

#

### 📌 02/21
#### ✔ 방을 나갈 경우와 새로고침 할 경우를 구분하여 입장 메시지 작성 구현 시작

#

### 📌 02/22
#### ✔ 방을 나갈 경우와 새로고침 할 경우를 구분하여 입장 메시지 작성 구현 중

#

### 📌 02/23
#### ✔ 방을 나갈 경우와 새로고침 할 경우를 구분하여 입장 메시지 작성 구현 완료

#

### 📌 03/02
#### ✔ 도배를 막기위해 채팅을 1초안에 5개 이상을 전송할 경우 5초간 채팅을 할 수 없도록 구현한 부분에서 에러가 발견되어 수정

#

### 📌 03/08
#### ✔ 방에 참여한 유저들의 프로필 이미지를 캐릭터화하여 캔버스 상에 다중 구현 및 실시간 움직임 구현

#

### 📌 03/15
#### ✔ 녹음 기능 구현 및 녹음된 파일을 웹소켓(STOMP)를 통해 채팅창에 전송하는 기능 구현

#

### 📌 03/20
#### ✔ 오디오 태그를 재생할 때 하나의 오디오 데이터는 문제가 없지만, 병합된 여러개의 오디오 데이터는 제대로 재생이 안 되던 문제 해결

#

### 📌 03/21
#### ✔ 전송한 녹음된 오디오 메시지를 전달받은 후 채팅 구역에 채팅 메시지 형태로 작성자 이름과 녹음된 오디오 파일을 함께 보여주도록 구현

#

### 📌 03/23
#### ✔ 방장이 퇴장할 경우 방이 터지기까지 1분의 유예 시간을 주고, 1분 이내에 방장이 다시 방에 들어올 경우 방은 유지되고, 들어오지 않을 경우 방이 터지도록 구현
#### ✔ 테이블 변경
##### Member 테이블에 시퀀스로 사용할 idx를 추가하였다.
##### Meta 테이블에 metaIdx를 idx로 변경하였다.
##### Meta 테이블과 MetaRoom 테이블에 각각 방장 닉네임을 받을 수 있는 metaMaster 컬럼을 추가하였다.

##### ~~사용된 데이터베이스 : MySQL - soju~~ (아래 수정된 테이블 사용)
	CREATE DATABASE soju;
	USE soju;

##### ~~사용된 테이블 : Member, Meta, MetaRoom~~ (아래 수정된 테이블 사용)
	멤버
	CREATE TABLE Member (
		idx BIGINT PRIMARY KEY AUTO_INCREMENT,
		##################회원가입 전 입력##################
		emailId VARCHAR(50) UNIQUE NOT NULL, #이메일 형식 아이디
		pwd VARCHAR(255) NOT NULL, #비밀번호
		name VARCHAR(10) NOT NULL, #이름
		nickname VARCHAR(20) UNIQUE NOT NULL, #닉네임
		birthday DATE NOT NULL, #생년월일
		gender VARCHAR(1) NOT NULL, #성별
		phoneNumber VARCHAR(15) UNIQUE NOT NULL, #핸드폰 번호
		address VARCHAR(100) NOT NULL, #주소
		studyType VARCHAR(10) NOT NULL, #관심있는 분야
		platform VARCHAR(10) NOT NULL, #플랫폼
		roleName VARCHAR(100) NOT NULL, #Spring Security 권한	
		##################회원가입 후 입력##################
		profileImage VARCHAR(100) #프로필 사진
	);
	
	#메타버스 방
	CREATE TABLE Meta(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #방 번호 - 기본키, 시퀀스
		metaTitle VARCHAR(50) NOT NULL, #방 제목
		metaType VARCHAR(10) NOT NULL, #방 분야
		metaPersonnel INT NOT NULL, #방 모집 인원
		metaRecruitingPersonnel INT NOT NULL, #방 참여중인 인원
		metaMaster VARCHAR(20) #방장 닉네임
	);

	#메타버스 방 내부
	CREATE TABLE MetaRoom(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #방 입장 번호(순서) - 기본키, 시퀀스
		metaIdx BIGINT NOT NULL, #방 번호
		CONSTRAINT fk_metaRoomIdx FOREIGN KEY(metaIdx) REFERENCES Meta(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		metaNickname VARCHAR(20) UNIQUE NOT NULL, #닉네임
		CONSTRAINT fk_metaRoomNickname FOREIGN KEY(metaNickname) REFERENCES Member(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		metaProfileImage VARCHAR(100), #프로필 사진
		metaMaster VARCHAR(20) UNIQUE #방장 닉네임
	);
#

### 📌 03/25
#### ✔ 새로고침과 퇴장을 구분 짓는 코드에서 자잘자잘하게 문제가 생기던 부분들 보완 작업 중

#

### 📌 03/26
#### ✔ 새로고침과 퇴장을 구분 짓는 코드에서 자잘자잘하게 문제가 생기던 부분들 보완 작업 완료
#### ✔ fetch를 사용하여 타 유저를 퇴장시킬 때 해당 유저의 세션까지는 제거를 못하던 문제 해결
##### fetch를 사용하여 타 유저를 퇴장시킬 때 해당 유저의 세션까지는 제거를 못 하기에,
##### 입장 메소드에서 세션이 존재하면 들어오는 재입장 구역에 재입장 체크를 하나 더 넣은 뒤,
##### 세션은 존재하는데 방 내부 참여자 명단에는 존재하지 않다면 여기서 가지고 있던 세션을 제거하고 다시 입장 메소드로 리다이렉트해 첫 입장이 되도록 하였다.

#

### 📌 03/28
#### ✔ 방장 위임 기능 및 참가자 강퇴 기능 구현
#### ✔ 테이블 변경
##### MetaRoom 테이블에 metaMaster(방장 닉네임) 컬럼을 Meta 테이블의 metaMaster(방장 닉네임) 컬럼과 외래키(FOREIGN KEY)로 연결하였다.
##### 이에 따라 Meta 테이블에서 metaMaster(방장 닉네임) 컬럼을 갱신하면 자동으로 MetaRoom 테이블의 metaMaster(방장 닉네임) 컬럼도 함께 갱신된다.
##### Member 테이블에 studyType 컬럼의 길이를 1 증가시켜 11로 만들었다.

##### ~~사용된 데이터베이스 : MySQL - soju~~ (아래 수정된 테이블 사용)
	CREATE DATABASE soju;
	USE soju;

##### ~~사용된 테이블 : Member, Meta, MetaRoom~~ (아래 수정된 테이블 사용)
	멤버 테이블
	CREATE TABLE Member (
		idx BIGINT PRIMARY KEY AUTO_INCREMENT,
		##################회원가입 전 입력##################
		emailId VARCHAR(50) UNIQUE NOT NULL, #이메일 형식 아이디
		pwd VARCHAR(255) NOT NULL, #비밀번호
		name VARCHAR(10) NOT NULL, #이름
		nickname VARCHAR(20) UNIQUE NOT NULL, #닉네임
		birthday DATE NOT NULL, #생년월일
		gender VARCHAR(1) NOT NULL, #성별
		phoneNumber VARCHAR(15) UNIQUE NOT NULL, #핸드폰 번호
		address VARCHAR(100) NOT NULL, #주소
		studyType VARCHAR(11) NOT NULL, #관심있는 분야
		platform VARCHAR(10) NOT NULL, #플랫폼
		roleName VARCHAR(100) NOT NULL, #Spring Security 권한	
		##################회원가입 후 입력##################
		profileImage VARCHAR(100) #프로필 사진
	);
	
	#메타버스 방
	CREATE TABLE Meta(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #방 번호 - 기본키, 시퀀스
		metaTitle VARCHAR(50) NOT NULL, #방 제목
		metaType VARCHAR(10) NOT NULL, #방 분야
		metaPersonnel INT NOT NULL, #방 모집 인원
		metaRecruitingPersonnel INT NOT NULL, #방 참여중인 인원
		metaMaster VARCHAR(20) UNIQUE #방장 닉네임
	);

	#메타버스 방 내부
	CREATE TABLE MetaRoom(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #방 입장 번호(순서) - 기본키, 시퀀스
		metaIdx BIGINT NOT NULL, #방 번호
		CONSTRAINT fk_metaRoomIdx FOREIGN KEY(metaIdx) REFERENCES Meta(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		metaNickname VARCHAR(20) UNIQUE NOT NULL, #닉네임
		CONSTRAINT fk_metaRoomNickname FOREIGN KEY(metaNickname) REFERENCES Member(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		metaProfileImage VARCHAR(100), #프로필 사진
		metaMaster VARCHAR(20), #방장 닉네임
		CONSTRAINT fk_metaRoomMaster FOREIGN KEY(metaMaster) REFERENCES Meta(metaMaster) ON DELETE CASCADE ON UPDATE CASCADE #포린키 연결
	);

#

### 📌 04/01
#### ✔ ID 찾기 및 PWD 찾기 추가
#### ✔ 회원가입 부가 기능들 추가
#### ✔ 각종 css 추가
#### ✔ 테이블 변경
##### Member 테이블에 detailAddress 컬럼을 추가하여 address 컬럼에서 한번에 받고 있던 주소값 중 상세 주소를 따로 받게 만들었다.
##### Member 테이블에 자기소개를 받을 selfIntro 컬럼을 추가하였다.

##### ~~사용된 데이터베이스 : MySQL - soju~~ (아래 수정된 테이블 사용)
	CREATE DATABASE soju;
	USE soju;

##### ~~사용된 테이블 : Member, Meta, MetaRoom~~ (아래 수정된 테이블 사용)
	#멤버 테이블
	CREATE TABLE Member (
		idx BIGINT PRIMARY KEY AUTO_INCREMENT,
		##################회원가입 전 입력##################
		emailId VARCHAR(50) UNIQUE NOT NULL, #이메일 형식 아이디
		pwd VARCHAR(255), #비밀번호
		name VARCHAR(10) NOT NULL, #이름
		nickname VARCHAR(10) UNIQUE NOT NULL, #닉네임
		birthday DATE NOT NULL, #생년월일
		gender VARCHAR(1) NOT NULL, #성별
		phoneNumber VARCHAR(15) UNIQUE NOT NULL, #핸드폰 번호
		address VARCHAR(100) NOT NULL, #주소
		detailAddress VARCHAR(100) NOT NULL, #상세주소
		studyType VARCHAR(11) NOT NULL, #관심있는 분야
		platform VARCHAR(10) NOT NULL, #플랫폼
		roleName VARCHAR(100) NOT NULL, #Spring Security 권한   
		##################회원가입 후 입력##################
		profileImage VARCHAR(100), #프로필 사진
		selfIntro VARCHAR(255) #자기소개
	);
	
	#메타버스 방
	CREATE TABLE Meta(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #방 번호 - 기본키, 시퀀스
		metaTitle VARCHAR(50) NOT NULL, #방 제목
		metaType VARCHAR(10) NOT NULL, #방 분야
		metaPersonnel INT NOT NULL, #방 모집 인원
		metaRecruitingPersonnel INT NOT NULL, #방 참여중인 인원
		metaMaster VARCHAR(20) UNIQUE #방장 닉네임
	);

	#메타버스 방 내부
	CREATE TABLE MetaRoom(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #방 입장 번호(순서) - 기본키, 시퀀스
		metaIdx BIGINT NOT NULL, #방 번호
		CONSTRAINT fk_metaRoomIdx FOREIGN KEY(metaIdx) REFERENCES Meta(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		metaNickname VARCHAR(20) UNIQUE NOT NULL, #닉네임
		CONSTRAINT fk_metaRoomNickname FOREIGN KEY(metaNickname) REFERENCES Member(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		metaProfileImage VARCHAR(100), #프로필 사진
		metaMaster VARCHAR(20), #방장 닉네임
		CONSTRAINT fk_metaRoomMaster FOREIGN KEY(metaMaster) REFERENCES Meta(metaMaster) ON DELETE CASCADE ON UPDATE CASCADE #포린키 연결
	);

#

### 📌 04/02
#### ✔ MyPage 및 회원정보 수정 기능 추가

#

### 📌 04/04
#### ✔ 이메일 인증 번호 암호화 기능 추가
#### ✔ SMTP에 사용되는 이메일 정보들을 properties로 분리
#### ✔ 스터디룸 완성 후 카페룸도 동일하게 구현 완료
#### ✔ 스터디룸과 카페룸 메시지 큐 분리 완료
#### ✔ 자습실은 페이지 구현은 완료되었고, 퇴장과 새로고침 구분 구현 중

#

### 📌 04/05
#### ✔ 자습실 STOMP 소켓을 사용하여 퇴장과 새로고침 구분 구현 완료
#### ✔ 스터디룸과 카페룸도 STOMP 소켓을 사용하여 방장 혼자일때 퇴장하는 경우 방 삭제되도록 구현 완료

#

### 📌 04/06
#### ✔ 퇴장과 새로고침 구분에서 새로고침을 너무 빠르게 연타할 경우 간간히 발생하던 에러들에 대해 예외 처리를 구현하여 에러 처리 완료
#### ✔ 새로고침 대기시간 0.1초에서 0.3초로 변경
#### ✔ 대기실과 검색 타입에서 자습실 제거 - 자습실은 대기실에 "혼자 공부하기" 버튼으로 자동 입장하는 1인실 방

#

### 📌 04/10
#### ✔ 세션 유지시간 24시간으로 변경
#### ✔ 카페룸과 자습실 css 추가

#

### 📌 04/11
#### ✔ 자습실 녹음 기능 및 나가기 버튼 추가
#### ✔ 자습실 녹음된 오디오 데이터 다운로드 기능 추가

#

### 📌 04/12
#### ✔ 스터디룸 및 카페룸 채팅창 높이 조절
#### ✔ 퇴장과 재입장(새로고침) 구분을 StompChatController에서 스레드(thread)로 처리하던 것을 MetaController에서 재입장(새로고침) 체크용 Map으로 처리하는 것으로 변경
#### ✔ 내부 js를 외부 js로 변경

#

### 📌 04/14
#### ✔ 3단 패키지명 soju에서 mate로 변경
#### ✔ 스터디원 모집 및 멘토 모집 및 멘티 모집 게시판 추가
#### ✔ 스토어 추가
#### ✔ 마이 페이지 찜 목록 및 현재 Mate 및 구매내역 추가
#### ✔ 메인 페이지 인기 모집글 리스트 기능 추가
#### ✔ 알람 기능 추가
#### ✔ 테이블 추가
##### 추가된 기능 및 페이지들에 맞게 테이블들을 추가하였다.

##### ~~사용된 데이터베이스 : MySQL - soju~~ (아래 수정된 데이터베이스 사용)
	CREATE DATABASE soju;
	USE soju;

##### ~~추가된 테이블 : RecruitStudy, RecruitStudyComment, RecruitStudyLike, RecruitMentee, RecruitMenteeComment, RecruitMenteeLike, RecruitMentor, RecruitMentorComment, RecruitMentorLike, Alarm, Meeting, Pay, Store, StoreComment, StoreLike~~ (아래 수정된 테이블 사용)
	#스터디원 모집
	CREATE TABLE RecruitStudy(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #게시글 번호 - 기본키, 시퀀스
		title VARCHAR(100) NOT NULL, #제목
		writeDate VARCHAR(20) NOT NULL, #작성일자
		writer VARCHAR(20) NOT NULL, #작성자
		CONSTRAINT fk_recruitStudyNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		studyType VARCHAR(20) NOT NULL, #분야
		personnel INT NOT NULL, #모집인원
		recruitingPersonnel INT NOT NULL, #모집된 인원
		recruiting INT NOT NULL, #모집 상태
		image VARCHAR(50) NOT NULL, #모집 사진
		studyIntro VARCHAR(500) NOT NULL, #스터디원 소개글
		studyLike INT NOT NULL, #찜 숫자
		studyLikeCheck INT NOT NULL # 찜 체크
	);

	#스터디원 모집 댓글
	CREATE TABLE RecruitStudyComment(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #댓글 번호 - 기본키, 시퀀스
		commentIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitStudyCommentIdx FOREIGN KEY(commentIdx) REFERENCES RecruitStudy(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		writeDate VARCHAR(50) NOT NULL, #작성일자
		writer VARCHAR(20) NOT NULL, #댓글 작성자
		CONSTRAINT fk_recruitStudyCommentNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		comment VARCHAR(100) NOT NULL, #댓글 내용
		deleteCheck INT(2) NOT NULL
	);

	#스터지원 모집 찜
	CREATE TABLE RecruitStudyLike(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #좋아요 번호 - 기본키, 시퀀스
		likeIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitStudyLikeIdx FOREIGN KEY(likeIdx) REFERENCES RecruitStudy(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		memberIdx BIGINT NOT NULL #유저 IDX
	);
	
	#멘티 모집
	CREATE TABLE RecruitMentee(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #게시글 번호 - 기본키, 시퀀스
		title VARCHAR(100), #제목
		writeDate VARCHAR(50), #작성일자
		writer VARCHAR(20), #작성자
		CONSTRAINT fk_recruitMenteeNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		studyType VARCHAR(20), #분야
		image VARCHAR(50), #대표 사진
		studyIntro VARCHAR(500), #본인 소개글
		recruiting INT, #모집 상태
		studyLike INT NOT NULL, #찜 숫자
		studyLikeCheck INT NOT NULL # 찜 확인
	);

	#멘티 모집 댓글
	CREATE TABLE RecruitMenteeComment(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #댓글 번호 - 기본키, 시퀀스
		commentIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitMenteeCommentIdx FOREIGN KEY(commentIdx) REFERENCES RecruitMentee(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		writeDate VARCHAR(50) NOT NULL, #작성일자
		writer VARCHAR(20) NOT NULL, #댓글 작성자
		CONSTRAINT fk_recruitMenteeCommentNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		comment VARCHAR(100) NOT NULL, #댓글 내용
		deleteCheck INT(2) NOT NULL
	);

	#멘티 모집 찜
	CREATE TABLE RecruitMenteeLike(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #좋아요 번호 - 기본키, 시퀀스
		likeIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitMenteeLikeIdx FOREIGN KEY(likeIdx) REFERENCES RecruitMentee(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		memberIdx BIGINT NOT NULL
	);

	#멘토 모집
	CREATE TABLE RecruitMentor(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #게시글 번호 - 기본키, 시퀀스
		title VARCHAR(100) NOT NULL, #제목
		writeDate VARCHAR(50) NOT NULL, #작성일자
		writer VARCHAR(20) NOT NULL, #작성자
		CONSTRAINT fk_recruitMentorNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		studyType VARCHAR(20) NOT NULL, #분야
		studyIntro VARCHAR(500) NOT NULL, #멘토 소개글
		recruiting INT NOT NULL, #모집 상태
		studyLike INT NOT NULL, #찜 숫자
		studyLikeCheck INT NOT NULL # 찜 체크
	);

	#멘토 모집 댓글
	CREATE TABLE RecruitMentorComment(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #댓글 번호 - 기본키, 시퀀스
		commentIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitMentorCommentIdx FOREIGN KEY(commentIdx) REFERENCES RecruitMentor(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		writeDate VARCHAR(50) NOT NULL, #작성일자 
		writer VARCHAR(20) NOT NULL, #댓글 작성자 
		CONSTRAINT fk_recruitMentorCommentNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		comment VARCHAR(100) NOT NULL, #댓글 내용
		deleteCheck INT(2) NOT NULL
	);

	#멘토 모집 찜
	CREATE TABLE RecruitMentorLike(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #좋아요 번호 - 기본키, 시퀀스
		likeIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitMentorLikeIdx FOREIGN KEY(likeIdx) REFERENCES RecruitMentor(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		memberIdx BIGINT NOT NULL #유저 IDX
	);

	#진행중인 소중한 만남
	CREATE TABLE Meeting(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #진행 번호 - 기본키, 시퀀스
		emailId VARCHAR(50) NOT NULL, #이메일 아이디 연결
		CONSTRAINT fk_meetingEmailId FOREIGN KEY(emailId) REFERENCES Sign(emailId) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		recruitStudyIdx BIGINT, #RecruitStudy idx 연결
		CONSTRAINT fk_meetingRecruitStudyIdx FOREIGN KEY(recruitStudyIdx) REFERENCES RecruitStudy(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		recruitStudyImage VARCHAR(50), #RecruitStudy 대표 사진
		recruitStudyTitle VARCHAR(100), #RecruitStudy 제목
		recruitMentorIdx BIGINT, #RecruitMentor idx 연결
		CONSTRAINT fk_meetingRecruitMentorIdx FOREIGN KEY(recruitMentorIdx) REFERENCES RecruitMentor(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		recruitMentorTitle VARCHAR(100), #RecruitMentor 제목
		recruitMentorWriter VARCHAR(20), #RecruitMentor 닉네임
		recruitMenteeIdx BIGINT, #MentorProfile idx 연결
		CONSTRAINT fk_meetingRecruitMenteeIdx FOREIGN KEY(recruitMenteeIdx) REFERENCES RecruitMentee(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		recruitMenteeImage VARCHAR(50), #MentorProfile 프로필 사진
		recruitMenteeTitle VARCHAR(100), #MentorProfile 제목
		recruitMenteeWriter VARCHAR(20) #MentorProfile 닉네임
	);

	#알람
	CREATE TABLE Alarm(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #알람 번호 - 기본키, 시퀀스
		emailId VARCHAR(50) NOT NULL, #수신자
		CONSTRAINT fk_alarmEmailId FOREIGN KEY(emailId) REFERENCES Sign(emailId) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		alarmType int(20) NOT NULL, #알람 타입(DTO에서 구분)
		nickname VARCHAR(20) NOT NULL, #발신자
		CONSTRAINT fk_alarmNickname FOREIGN KEY(nickname) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		title VARCHAR(100), #제목
		recruitStudyIdx BIGINT,
		CONSTRAINT fk_alarmRecruitStudyIdx FOREIGN KEY(recruitStudyIdx) REFERENCES RecruitStudy(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 스터디원 포린키 연결
		recruitMentorIdx BIGINT,
		CONSTRAINT fk_alarmRecruitMentorIdx FOREIGN KEY(recruitMentorIdx) REFERENCES RecruitMentor(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 멘토 포린키 연결
		recruitMenteeIdx BIGINT,
		CONSTRAINT fk_alarmLiketRecruitMenteeIdx FOREIGN KEY(recruitMenteeIdx) REFERENCES RecruitMentee(idx) ON DELETE CASCADE ON UPDATE CASCADE # 멘토 프로필 포린키 연결
	);

	#결제
	CREATE TABLE Pay(
		impUid VARCHAR(200) PRIMARY KEY, #결제 고유번호 - 기본키
		merchantUid VARCHAR(200) NOT NULL,
		PGName VARCHAR(50) NOT NULL, 
		payMethod VARCHAR(300),
		itemName VARCHAR(50) NOT NULL,
		price INT NOT NULL,
		buyerEmail VARCHAR(50) NOT NULL,
		CONSTRAINT fk_payEmailId FOREIGN KEY(buyerEmail) REFERENCES Sign(emailId) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		buyerName VARCHAR(20) NOT NULL,
		buyerTel VARCHAR(20) NOT NULL,
		buyerAddress VARCHAR(50),
		buyerPostNum VARCHAR(50),
		itemCount INT NOT NULL,
		isPaid INT NOT NULL
	);

	#스토어
	CREATE TABLE Store(
		storeIdx BIGINT PRIMARY KEY AUTO_INCREMENT, #스토어 번호 - 기본키, 시퀀스
		goods VARCHAR(50) NOT NULL,
		category VARCHAR(50) NOT NULL,
		price INT NOT NULL,
		introduce VARCHAR(500),
		stock INT(10) NOT NULL,
		goodsLike INT NOT NULL,
		itemName VARCHAR(50) NOT NULL,
		image VARCHAR(50) NOT NULL
	);

	#스토어 댓글
	CREATE TABLE StoreComment(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #댓글 번호 - 기본키, 시퀀스
		commentIdx BIGINT,
		CONSTRAINT fk_storeCommentIdx FOREIGN KEY(commentIdx) REFERENCES Store(storeIdx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		writer VARCHAR(20),
		comment VARCHAR(100),
		deleteCheck INT(2)
	);

	#스토어 좋아요
	CREATE TABLE StoreLike(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #좋아요 번호 - 기본키, 시퀀스
		likeIdx BIGINT NOT NULL,
		CONSTRAINT fk_storeLikeIdx FOREIGN KEY(likeIdx) REFERENCES Store(storeIdx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		memberIdx BIGINT NOT NULL
	);
	
#

### 📌 04/15
#### ✔ 소셜 가입자는 비밀번호를 변경할 수 없도록 수정
#### ✔ DB 이름 soju에서 Mate로 변경
##### 프로젝트 이름을 soju에서 Mate로 변경하였기에 DB 이름도 Mate로 변경하였다.

##### ~~데이터베이스 : MySQL - Mate~~ (아래 수정된 테이블 사용)
	CREATE DATABASE Mate DEFAULT CHARACTER SET utf8; #default character set - 한글 깨짐 방지 기능
	USE Mate;
	
#### ✔ 테이블 이름 Member에서 Sign으로 변경
##### MySQL에 Member라는 이름이 예약어로 있었기에 안전하게 Sign으로 변경하였다.

##### ~~테이블 : Sign, Meta, MetaRoom, CheckList, RecruitStudy, RecruitStudyComment, RecruitStudyLike, RecruitMentee, RecruitMenteeComment, RecruitMenteeLike, RecruitMentor, RecruitMentorComment, RecruitMentorLike, Alarm, Meeting, Pay, Store, StoreComment, StoreLike~~ (아래 수정된 테이블 사용)
	#멤버
	CREATE TABLE Sign(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #유저 번호 - 기본키, 시퀀스
		##################회원가입 전 입력##################
		emailId VARCHAR(50) UNIQUE NOT NULL, #이메일 형식 아이디
		pwd VARCHAR(255), #비밀번호
		name VARCHAR(10) NOT NULL, #이름
		nickname VARCHAR(10) UNIQUE NOT NULL, #닉네임
		birthday DATE NOT NULL, #생년월일
		gender VARCHAR(1) NOT NULL, #성별
		phoneNumber VARCHAR(15) UNIQUE NOT NULL, #핸드폰 번호
		address VARCHAR(100) NOT NULL, #주소
		detailAddress VARCHAR(100) NOT NULL, #상세주소
		studyType VARCHAR(11) NOT NULL, #관심있는 분야
		platform VARCHAR(10) NOT NULL, #플랫폼
		roleName VARCHAR(100) NOT NULL, #Spring Security 권한   
		##################회원가입 후 입력##################
		profileImage VARCHAR(100), #프로필 사진
		selfIntro VARCHAR(255) #자기소개
	);

	#메타버스 방
	CREATE TABLE Meta(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #방 번호 - 기본키, 시퀀스
		metaTitle VARCHAR(50) NOT NULL, #방 제목
		metaType VARCHAR(10) NOT NULL, #방 분야
		metaPersonnel INT NOT NULL, #방 모집 인원
		metaRecruitingPersonnel INT NOT NULL, #방 참여중인 인원
		metaMaster VARCHAR(20) UNIQUE #방장 닉네임
	);

	#메타버스 방 내부
	CREATE TABLE MetaRoom(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #방 입장 번호(순서) - 기본키, 시퀀스
		metaIdx BIGINT NOT NULL, #방 번호
		CONSTRAINT fk_metaRoomIdx FOREIGN KEY(metaIdx) REFERENCES Meta(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		metaNickname VARCHAR(20) UNIQUE NOT NULL, #닉네임
		CONSTRAINT fk_metaRoomNickname FOREIGN KEY(metaNickname) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		metaProfileImage VARCHAR(100), #프로필 사진
		metaMaster VARCHAR(20), #방장 닉네임
		CONSTRAINT fk_metaRoomMaster FOREIGN KEY(metaMaster) REFERENCES Meta(metaMaster) ON DELETE CASCADE ON UPDATE CASCADE #포린키 연결
	);

	#스터디원 모집
	CREATE TABLE RecruitStudy(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #게시글 번호 - 기본키, 시퀀스
		title VARCHAR(100) NOT NULL, #제목
		writeDate VARCHAR(20) NOT NULL, #작성일자
		writer VARCHAR(20) NOT NULL, #작성자
		CONSTRAINT fk_recruitStudyNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		studyType VARCHAR(20) NOT NULL, #분야
		personnel INT NOT NULL, #모집인원
		recruitingPersonnel INT NOT NULL, #모집된 인원
		recruiting INT NOT NULL, #모집 상태
		image VARCHAR(50) NOT NULL, #모집 사진
		studyIntro VARCHAR(500) NOT NULL, #스터디원 소개글
		studyLike INT NOT NULL, #찜 숫자
		studyLikeCheck INT NOT NULL # 찜 체크
	);

	#스터디원 모집 댓글
	CREATE TABLE RecruitStudyComment(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #댓글 번호 - 기본키, 시퀀스
		commentIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitStudyCommentIdx FOREIGN KEY(commentIdx) REFERENCES RecruitStudy(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		writeDate VARCHAR(50) NOT NULL, #작성일자
		writer VARCHAR(20) NOT NULL, #댓글 작성자
		CONSTRAINT fk_recruitStudyCommentNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		comment VARCHAR(100) NOT NULL, #댓글 내용
		deleteCheck INT(2) NOT NULL
	);

	#스터지원 모집 찜
	CREATE TABLE RecruitStudyLike(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #좋아요 번호 - 기본키, 시퀀스
		likeIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitStudyLikeIdx FOREIGN KEY(likeIdx) REFERENCES RecruitStudy(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		memberIdx BIGINT NOT NULL #유저 IDX
	);
	
	#멘티 모집
	CREATE TABLE RecruitMentee(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #게시글 번호 - 기본키, 시퀀스
		title VARCHAR(100), #제목
		writeDate VARCHAR(50), #작성일자
		writer VARCHAR(20), #작성자
		CONSTRAINT fk_recruitMenteeNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		studyType VARCHAR(20), #분야
		image VARCHAR(50), #대표 사진
		studyIntro VARCHAR(500), #본인 소개글
		recruiting INT, #모집 상태
		studyLike INT NOT NULL, #찜 숫자
		studyLikeCheck INT NOT NULL # 찜 확인
	);

	#멘티 모집 댓글
	CREATE TABLE RecruitMenteeComment(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #댓글 번호 - 기본키, 시퀀스
		commentIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitMenteeCommentIdx FOREIGN KEY(commentIdx) REFERENCES RecruitMentee(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		writeDate VARCHAR(50) NOT NULL, #작성일자
		writer VARCHAR(20) NOT NULL, #댓글 작성자
		CONSTRAINT fk_recruitMenteeCommentNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		comment VARCHAR(100) NOT NULL, #댓글 내용
		deleteCheck INT(2) NOT NULL
	);

	#멘티 모집 찜
	CREATE TABLE RecruitMenteeLike(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #좋아요 번호 - 기본키, 시퀀스
		likeIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitMenteeLikeIdx FOREIGN KEY(likeIdx) REFERENCES RecruitMentee(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		memberIdx BIGINT NOT NULL
	);

	#멘토 모집
	CREATE TABLE RecruitMentor(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #게시글 번호 - 기본키, 시퀀스
		title VARCHAR(100) NOT NULL, #제목
		writeDate VARCHAR(50) NOT NULL, #작성일자
		writer VARCHAR(20) NOT NULL, #작성자
		CONSTRAINT fk_recruitMentorNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		studyType VARCHAR(20) NOT NULL, #분야
		studyIntro VARCHAR(500) NOT NULL, #멘토 소개글
		recruiting INT NOT NULL, #모집 상태
		studyLike INT NOT NULL, #찜 숫자
		studyLikeCheck INT NOT NULL # 찜 체크
	);

	#멘토 모집 댓글
	CREATE TABLE RecruitMentorComment(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #댓글 번호 - 기본키, 시퀀스
		commentIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitMentorCommentIdx FOREIGN KEY(commentIdx) REFERENCES RecruitMentor(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		writeDate VARCHAR(50) NOT NULL, #작성일자 
		writer VARCHAR(20) NOT NULL, #댓글 작성자 
		CONSTRAINT fk_recruitMentorCommentNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		comment VARCHAR(100) NOT NULL, #댓글 내용
		deleteCheck INT(2) NOT NULL
	);

	#멘토 모집 찜
	CREATE TABLE RecruitMentorLike(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #좋아요 번호 - 기본키, 시퀀스
		likeIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitMentorLikeIdx FOREIGN KEY(likeIdx) REFERENCES RecruitMentor(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		memberIdx BIGINT NOT NULL #유저 IDX
	);

	#진행중인 소중한 만남
	CREATE TABLE Meeting(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #진행 번호 - 기본키, 시퀀스
		emailId VARCHAR(50) NOT NULL, #이메일 아이디 연결
		CONSTRAINT fk_meetingEmailId FOREIGN KEY(emailId) REFERENCES Sign(emailId) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		recruitStudyIdx BIGINT, #RecruitStudy idx 연결
		CONSTRAINT fk_meetingRecruitStudyIdx FOREIGN KEY(recruitStudyIdx) REFERENCES RecruitStudy(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		recruitStudyImage VARCHAR(50), #RecruitStudy 대표 사진
		recruitStudyTitle VARCHAR(100), #RecruitStudy 제목
		recruitMentorIdx BIGINT, #RecruitMentor idx 연결
		CONSTRAINT fk_meetingRecruitMentorIdx FOREIGN KEY(recruitMentorIdx) REFERENCES RecruitMentor(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		recruitMentorTitle VARCHAR(100), #RecruitMentor 제목
		recruitMentorWriter VARCHAR(20), #RecruitMentor 닉네임
		recruitMenteeIdx BIGINT, #MentorProfile idx 연결
		CONSTRAINT fk_meetingRecruitMenteeIdx FOREIGN KEY(recruitMenteeIdx) REFERENCES RecruitMentee(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		recruitMenteeImage VARCHAR(50), #MentorProfile 프로필 사진
		recruitMenteeTitle VARCHAR(100), #MentorProfile 제목
		recruitMenteeWriter VARCHAR(20) #MentorProfile 닉네임
	);

	#알람
	CREATE TABLE Alarm(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #알람 번호 - 기본키, 시퀀스
		emailId VARCHAR(50) NOT NULL, #수신자
		CONSTRAINT fk_alarmEmailId FOREIGN KEY(emailId) REFERENCES Sign(emailId) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		alarmType int(20) NOT NULL, #알람 타입(DTO에서 구분)
		nickname VARCHAR(20) NOT NULL, #발신자
		CONSTRAINT fk_alarmNickname FOREIGN KEY(nickname) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		title VARCHAR(100), #제목
		recruitStudyIdx BIGINT,
		CONSTRAINT fk_alarmRecruitStudyIdx FOREIGN KEY(recruitStudyIdx) REFERENCES RecruitStudy(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 스터디원 포린키 연결
		recruitMentorIdx BIGINT,
		CONSTRAINT fk_alarmRecruitMentorIdx FOREIGN KEY(recruitMentorIdx) REFERENCES RecruitMentor(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 멘토 포린키 연결
		recruitMenteeIdx BIGINT,
		CONSTRAINT fk_alarmLiketRecruitMenteeIdx FOREIGN KEY(recruitMenteeIdx) REFERENCES RecruitMentee(idx) ON DELETE CASCADE ON UPDATE CASCADE # 멘토 프로필 포린키 연결
	);

	#결제
	CREATE TABLE Pay(
		impUid VARCHAR(200) PRIMARY KEY, #결제 고유번호 - 기본키
		merchantUid VARCHAR(200) NOT NULL,
		PGName VARCHAR(50) NOT NULL, 
		payMethod VARCHAR(300),
		itemName VARCHAR(50) NOT NULL,
		price INT NOT NULL,
		buyerEmail VARCHAR(50) NOT NULL,
		CONSTRAINT fk_payEmailId FOREIGN KEY(buyerEmail) REFERENCES Sign(emailId) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		buyerName VARCHAR(20) NOT NULL,
		buyerTel VARCHAR(20) NOT NULL,
		buyerAddress VARCHAR(50),
		buyerPostNum VARCHAR(50),
		itemCount INT NOT NULL,
		isPaid INT NOT NULL
	);

	#스토어
	CREATE TABLE Store(
		storeIdx BIGINT PRIMARY KEY AUTO_INCREMENT, #스토어 번호 - 기본키, 시퀀스
		goods VARCHAR(50) NOT NULL,
		category VARCHAR(50) NOT NULL,
		price INT NOT NULL,
		introduce VARCHAR(500),
		stock INT(10) NOT NULL,
		goodsLike INT NOT NULL,
		itemName VARCHAR(50) NOT NULL,
		image VARCHAR(50) NOT NULL
	);

	#스토어 댓글
	CREATE TABLE StoreComment(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #댓글 번호 - 기본키, 시퀀스
		commentIdx BIGINT,
		CONSTRAINT fk_storeCommentIdx FOREIGN KEY(commentIdx) REFERENCES Store(storeIdx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		writer VARCHAR(20),
		comment VARCHAR(100),
		deleteCheck INT(2)
	);

	#스토어 좋아요
	CREATE TABLE StoreLike(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #좋아요 번호 - 기본키, 시퀀스
		likeIdx BIGINT NOT NULL,
		CONSTRAINT fk_storeLikeIdx FOREIGN KEY(likeIdx) REFERENCES Store(storeIdx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		memberIdx BIGINT NOT NULL
	);
	
#

### 📌 04/18
#### ✔ 퇴장 처리하는 위치를 클라이언트에서 서버로 변경
#### ✔ 방장의 퇴장을 처리하는 위치도 클라이언트에서 서버로 변경
##### 클라이언트에서 fetch로 퇴장 처리하던 것을 StompChatController에서 방 퇴장 메소드를 직접 호출하여 퇴장 처리하는 것으로 변경하였다.
#### ✔ 최근 입장한 방 목록 기능 구현
#### ✔ 테이블 추가
##### 최근 입장한 방을 체크하고 보여줄 EnterMeta 테이블을 추가하였다.

##### 데이터베이스 : MySQL - Mate
	CREATE DATABASE Mate DEFAULT CHARACTER SET utf8; #default character set - 한글 깨짐 방지 기능
	USE Mate;

##### 테이블 : Sign, Meta, MetaRoom, EnterMeta, RecruitStudy, RecruitStudyComment, RecruitStudyLike, RecruitMentee, RecruitMenteeComment, RecruitMenteeLike, RecruitMentor, RecruitMentorComment, RecruitMentorLike, Alarm, Meeting, Pay, Store, StoreComment, StoreLike
	#멤버
	CREATE TABLE Sign(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #유저 번호 - 기본키, 시퀀스
		##################회원가입 전 입력##################
		emailId VARCHAR(50) UNIQUE NOT NULL, #이메일 형식 아이디
		pwd VARCHAR(255), #비밀번호
		name VARCHAR(10) NOT NULL, #이름
		nickname VARCHAR(10) UNIQUE NOT NULL, #닉네임
		birthday DATE NOT NULL, #생년월일
		gender VARCHAR(1) NOT NULL, #성별
		phoneNumber VARCHAR(15) UNIQUE NOT NULL, #핸드폰 번호
		address VARCHAR(100) NOT NULL, #주소
		detailAddress VARCHAR(100) NOT NULL, #상세주소
		studyType VARCHAR(11) NOT NULL, #관심있는 분야
		platform VARCHAR(10) NOT NULL, #플랫폼
		roleName VARCHAR(100) NOT NULL, #Spring Security 권한   
		##################회원가입 후 입력##################
		profileImage VARCHAR(100), #프로필 사진
		selfIntro VARCHAR(255) #자기소개
	);

	#메타버스 방
	CREATE TABLE Meta(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #방 번호 - 기본키, 시퀀스
		metaTitle VARCHAR(50) NOT NULL, #방 제목
		metaType VARCHAR(10) NOT NULL, #방 분야
		metaPersonnel INT NOT NULL, #방 모집 인원
		metaRecruitingPersonnel INT NOT NULL, #방 참여중인 인원
		metaMaster VARCHAR(20) UNIQUE #방장 닉네임
	);

	#메타버스 방 내부
	CREATE TABLE MetaRoom(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #방 입장 번호(순서) - 기본키, 시퀀스
		metaIdx BIGINT NOT NULL, #방 번호
		CONSTRAINT fk_metaRoomIdx FOREIGN KEY(metaIdx) REFERENCES Meta(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		metaNickname VARCHAR(20) UNIQUE NOT NULL, #닉네임
		CONSTRAINT fk_metaRoomNickname FOREIGN KEY(metaNickname) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		metaProfileImage VARCHAR(100), #프로필 사진
		metaMaster VARCHAR(20), #방장 닉네임
		CONSTRAINT fk_metaRoomMaster FOREIGN KEY(metaMaster) REFERENCES Meta(metaMaster) ON DELETE CASCADE ON UPDATE CASCADE #포린키 연결
	);
	
	#메타버스 최근 입장 방
	CREATE TABLE EnterMeta(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #최근 방 입장 번호 - 기본키, 시퀀스
		metaNickname VARCHAR(10) NOT NULL, #이메일 아이디
		CONSTRAINT fk_enterMetaNickname FOREIGN KEY(metaNickname) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		metaIdx BIGINT NOT NULL, #방 번호
		CONSTRAINT fk_enterMetaIdx FOREIGN KEY(metaIdx) REFERENCES Meta(idx) ON DELETE CASCADE ON UPDATE CASCADE #포린키 연결
	);

	#스터디원 모집
	CREATE TABLE RecruitStudy(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #게시글 번호 - 기본키, 시퀀스
		title VARCHAR(100) NOT NULL, #제목
		writeDate VARCHAR(20) NOT NULL, #작성일자
		writer VARCHAR(20) NOT NULL, #작성자
		CONSTRAINT fk_recruitStudyNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		studyType VARCHAR(20) NOT NULL, #분야
		personnel INT NOT NULL, #모집인원
		recruitingPersonnel INT NOT NULL, #모집된 인원
		recruiting INT NOT NULL, #모집 상태
		image VARCHAR(50) NOT NULL, #모집 사진
		studyIntro VARCHAR(500) NOT NULL, #스터디원 소개글
		studyLike INT NOT NULL, #찜 숫자
		studyLikeCheck INT NOT NULL # 찜 체크
	);

	#스터디원 모집 댓글
	CREATE TABLE RecruitStudyComment(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #댓글 번호 - 기본키, 시퀀스
		commentIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitStudyCommentIdx FOREIGN KEY(commentIdx) REFERENCES RecruitStudy(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		writeDate VARCHAR(50) NOT NULL, #작성일자
		writer VARCHAR(20) NOT NULL, #댓글 작성자
		CONSTRAINT fk_recruitStudyCommentNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		comment VARCHAR(100) NOT NULL, #댓글 내용
		deleteCheck INT(2) NOT NULL
	);

	#스터지원 모집 찜
	CREATE TABLE RecruitStudyLike(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #좋아요 번호 - 기본키, 시퀀스
		likeIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitStudyLikeIdx FOREIGN KEY(likeIdx) REFERENCES RecruitStudy(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		memberIdx BIGINT NOT NULL #유저 IDX
	);
	
	#멘티 모집
	CREATE TABLE RecruitMentee(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #게시글 번호 - 기본키, 시퀀스
		title VARCHAR(100), #제목
		writeDate VARCHAR(50), #작성일자
		writer VARCHAR(20), #작성자
		CONSTRAINT fk_recruitMenteeNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		studyType VARCHAR(20), #분야
		image VARCHAR(50), #대표 사진
		studyIntro VARCHAR(500), #본인 소개글
		recruiting INT, #모집 상태
		studyLike INT NOT NULL, #찜 숫자
		studyLikeCheck INT NOT NULL # 찜 확인
	);

	#멘티 모집 댓글
	CREATE TABLE RecruitMenteeComment(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #댓글 번호 - 기본키, 시퀀스
		commentIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitMenteeCommentIdx FOREIGN KEY(commentIdx) REFERENCES RecruitMentee(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		writeDate VARCHAR(50) NOT NULL, #작성일자
		writer VARCHAR(20) NOT NULL, #댓글 작성자
		CONSTRAINT fk_recruitMenteeCommentNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		comment VARCHAR(100) NOT NULL, #댓글 내용
		deleteCheck INT(2) NOT NULL
	);

	#멘티 모집 찜
	CREATE TABLE RecruitMenteeLike(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #좋아요 번호 - 기본키, 시퀀스
		likeIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitMenteeLikeIdx FOREIGN KEY(likeIdx) REFERENCES RecruitMentee(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		memberIdx BIGINT NOT NULL
	);

	#멘토 모집
	CREATE TABLE RecruitMentor(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #게시글 번호 - 기본키, 시퀀스
		title VARCHAR(100) NOT NULL, #제목
		writeDate VARCHAR(50) NOT NULL, #작성일자
		writer VARCHAR(20) NOT NULL, #작성자
		CONSTRAINT fk_recruitMentorNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		studyType VARCHAR(20) NOT NULL, #분야
		studyIntro VARCHAR(500) NOT NULL, #멘토 소개글
		recruiting INT NOT NULL, #모집 상태
		studyLike INT NOT NULL, #찜 숫자
		studyLikeCheck INT NOT NULL # 찜 체크
	);

	#멘토 모집 댓글
	CREATE TABLE RecruitMentorComment(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #댓글 번호 - 기본키, 시퀀스
		commentIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitMentorCommentIdx FOREIGN KEY(commentIdx) REFERENCES RecruitMentor(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		writeDate VARCHAR(50) NOT NULL, #작성일자 
		writer VARCHAR(20) NOT NULL, #댓글 작성자 
		CONSTRAINT fk_recruitMentorCommentNickname FOREIGN KEY(writer) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		comment VARCHAR(100) NOT NULL, #댓글 내용
		deleteCheck INT(2) NOT NULL
	);

	#멘토 모집 찜
	CREATE TABLE RecruitMentorLike(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #좋아요 번호 - 기본키, 시퀀스
		likeIdx BIGINT NOT NULL, #게시글 번호
		CONSTRAINT fk_recruitMentorLikeIdx FOREIGN KEY(likeIdx) REFERENCES RecruitMentor(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		memberIdx BIGINT NOT NULL #유저 IDX
	);

	#진행중인 소중한 만남
	CREATE TABLE Meeting(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #진행 번호 - 기본키, 시퀀스
		emailId VARCHAR(50) NOT NULL, #이메일 아이디 연결
		CONSTRAINT fk_meetingEmailId FOREIGN KEY(emailId) REFERENCES Sign(emailId) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		recruitStudyIdx BIGINT, #RecruitStudy idx 연결
		CONSTRAINT fk_meetingRecruitStudyIdx FOREIGN KEY(recruitStudyIdx) REFERENCES RecruitStudy(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		recruitStudyImage VARCHAR(50), #RecruitStudy 대표 사진
		recruitStudyTitle VARCHAR(100), #RecruitStudy 제목
		recruitMentorIdx BIGINT, #RecruitMentor idx 연결
		CONSTRAINT fk_meetingRecruitMentorIdx FOREIGN KEY(recruitMentorIdx) REFERENCES RecruitMentor(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		recruitMentorTitle VARCHAR(100), #RecruitMentor 제목
		recruitMentorWriter VARCHAR(20), #RecruitMentor 닉네임
		recruitMenteeIdx BIGINT, #MentorProfile idx 연결
		CONSTRAINT fk_meetingRecruitMenteeIdx FOREIGN KEY(recruitMenteeIdx) REFERENCES RecruitMentee(idx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		recruitMenteeImage VARCHAR(50), #MentorProfile 프로필 사진
		recruitMenteeTitle VARCHAR(100), #MentorProfile 제목
		recruitMenteeWriter VARCHAR(20) #MentorProfile 닉네임
	);

	#알람
	CREATE TABLE Alarm(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #알람 번호 - 기본키, 시퀀스
		emailId VARCHAR(50) NOT NULL, #수신자
		CONSTRAINT fk_alarmEmailId FOREIGN KEY(emailId) REFERENCES Sign(emailId) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		alarmType int(20) NOT NULL, #알람 타입(DTO에서 구분)
		nickname VARCHAR(20) NOT NULL, #발신자
		CONSTRAINT fk_alarmNickname FOREIGN KEY(nickname) REFERENCES Sign(nickname) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		title VARCHAR(100), #제목
		recruitStudyIdx BIGINT,
		CONSTRAINT fk_alarmRecruitStudyIdx FOREIGN KEY(recruitStudyIdx) REFERENCES RecruitStudy(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 스터디원 포린키 연결
		recruitMentorIdx BIGINT,
		CONSTRAINT fk_alarmRecruitMentorIdx FOREIGN KEY(recruitMentorIdx) REFERENCES RecruitMentor(idx) ON DELETE CASCADE ON UPDATE CASCADE, # 멘토 포린키 연결
		recruitMenteeIdx BIGINT,
		CONSTRAINT fk_alarmLiketRecruitMenteeIdx FOREIGN KEY(recruitMenteeIdx) REFERENCES RecruitMentee(idx) ON DELETE CASCADE ON UPDATE CASCADE # 멘토 프로필 포린키 연결
	);

	#결제
	CREATE TABLE Pay(
		impUid VARCHAR(200) PRIMARY KEY, #결제 고유번호 - 기본키
		merchantUid VARCHAR(200) NOT NULL,
		PGName VARCHAR(50) NOT NULL, 
		payMethod VARCHAR(300),
		itemName VARCHAR(50) NOT NULL,
		price INT NOT NULL,
		buyerEmail VARCHAR(50) NOT NULL,
		CONSTRAINT fk_payEmailId FOREIGN KEY(buyerEmail) REFERENCES Sign(emailId) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		buyerName VARCHAR(20) NOT NULL,
		buyerTel VARCHAR(20) NOT NULL,
		buyerAddress VARCHAR(50),
		buyerPostNum VARCHAR(50),
		itemCount INT NOT NULL,
		isPaid INT NOT NULL
	);

	#스토어
	CREATE TABLE Store(
		storeIdx BIGINT PRIMARY KEY AUTO_INCREMENT, #스토어 번호 - 기본키, 시퀀스
		goods VARCHAR(50) NOT NULL,
		category VARCHAR(50) NOT NULL,
		price INT NOT NULL,
		introduce VARCHAR(500),
		stock INT(10) NOT NULL,
		goodsLike INT NOT NULL,
		itemName VARCHAR(50) NOT NULL,
		image VARCHAR(50) NOT NULL
	);

	#스토어 댓글
	CREATE TABLE StoreComment(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #댓글 번호 - 기본키, 시퀀스
		commentIdx BIGINT,
		CONSTRAINT fk_storeCommentIdx FOREIGN KEY(commentIdx) REFERENCES Store(storeIdx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		writer VARCHAR(20),
		comment VARCHAR(100),
		deleteCheck INT(2)
	);

	#스토어 좋아요
	CREATE TABLE StoreLike(
		idx BIGINT PRIMARY KEY AUTO_INCREMENT, #좋아요 번호 - 기본키, 시퀀스
		likeIdx BIGINT NOT NULL,
		CONSTRAINT fk_storeLikeIdx FOREIGN KEY(likeIdx) REFERENCES Store(storeIdx) ON DELETE CASCADE ON UPDATE CASCADE, #포린키 연결
		memberIdx BIGINT NOT NULL
	);

#

### 📌 04/19
#### ✔ 재입장(새로고침) 하는 경우 참가자란에 새로 참가자 정보를 작성하던 것을 첫 입장때 작성한 참가자 정보를 유지하는 것으로 변경
##### 재입장(새로고침)을 할 때마다 서버에서는 db에 새로 참가자 정보를 저장하였고, 클라이언트에서는 참가자란에 새로 참가자 정보 작성하여서, 재입장(새로고침)도 새로 입장한 것처럼 보이도록 하였다.
##### 하지만 재입장(새로고침)을 할 때는 굳이 그렇게까지 할 필요가 없어 보여 첫 입장 때 작성한 정보를 그대로 유지하도록 변경하였다.

#
