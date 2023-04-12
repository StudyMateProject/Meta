//////////////////////////////////////////////////// 소켓 구역 ////////////////////////////////////////////////////
// 1. SockJS를 생성한다. (StompWebSocketConfig에서 설정한 Endpoint와 동일하게 맞춰준다.)
var sockJs = new SockJS("/ws/meta/chat");
// 2. 생성된 SockJS를 Stomp에 전달한다.
var stomp = Stomp.over(sockJs);

// 3. connect가 이뤄지면 실행한다.
stomp.connect({}, function () {
    // connect가 이뤄지면 콘솔에 로그를 찍는다.
    console.log("STOMP Connection");

    // 5. subscribe(path, callback)으로 메시지를 받을 수 있다.
    //    StompChatController에서 SimpMessagingTemplate를 통해 전달한 DTO를 여기서 콜백 메소드 파라미터로 전달 받는다.
    stomp.subscribe("/sub/meta/studyroom/" + metaIdx, function (chat) {
        // 5-1. 채팅에 필요한 것들
        var chatArea = document.getElementById("chatArea"); // 메시지가 작성될 채팅 구역
        var str = ""; // 메시지 코드가 작성될 변수
        // 6. 5에서 전달받은 메시지의 headers에서 type 값이 "record"인지 체크한다.
        // 6-1. type 값이 "record"인 경우
        if ( chat.headers.type == "record" ) {
            //////////////////////////////////////////전달 받은 녹음 올리기//////////////////////////////////////////
            // 7. document.createElement()를 사용하여, 전달 받은 녹음된 오디오 메시지를 코드로 작성해 채팅 구역에 올린다.
            // document.createElement() - 지정한 태그 이름을 가진 HTML 요소를 생성하는 메소드이다.
            //                            이 메소드를 사용하면 동적으로 HTML 요소를 생성할 수 있다.

            // 1. audio(오디오) 태그를 생성한다.
            var audioTag = document.createElement("audio");
            // 1-1. 5에서 전달 받은 메시지의 body에서 녹음된 오디오 데이터를 가져와 audio(오디오) 태그의 소스로 전달한다.
            audioTag.src = chat.body;
            // 1-2. audio(오디오) 태그의 컨트롤러를 설정한다.
            audioTag.controls = true;

            // 2. b 태그를 생성한다.
            var bTag = document.createElement("b");
            // 2-1. b 태그의 display를 flex로 설정하는 style을 추가한다.
            bTag.style.display = "flex";
            // 2-2. b 태그의 align-items를 center로 설정하는 style을 추가한다.
            bTag.style.alignItems = "center";
            // document.createTextNode() - 지정한 텍스트를 포함하는 새로운 Text 노드를 생성하는 메소드이다.
            //                             이 메소드를 사용하여 동적으로 HTML 요소의 텍스트를 업데이트할 수 있다.
            //                             이 메소드는 document.createElement()와 함께 사용하여 동적으로 HTML 요소를 생성하는 데 사용된다.
            // 2-3. b 태그에 작성할 내용을 TextNode로 생성한다.
            var bContent = document.createTextNode(chat.headers.writer + " : ");
            // 2-4. b 태그에 TextNode로 생성한 내용을 전달한다.
            bTag.appendChild(bContent);
            // 2-5. b 태그에 audio(오디오) 태그를 전달한다.
            bTag.appendChild(audioTag);

            // 3. div 태그를 생성한다.
            var divTag = document.createElement("div");
            // 3-1. div 태그의 text-align을 left로 설정하는 style을 추가한다.
            divTag.style.textAlign = "left";
            // 3-2. div 태그에 b 태그를 전달한다.
            divTag.appendChild(bTag);

            // 4. 채팅 구역에 div 태그를 올린다.
            chatArea.appendChild(divTag);

            // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
            chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;
            ////////////////////////////////////////// 전달 받은 녹음 재생 //////////////////////////////////////////
            // 8. 채팅 구역에 올라가 있는 audio(오디오) 태그를 모두 선택하여 audioTags 변수에 NodeList 형태로 저장한다.
            // querySelectorAll - HTML 문서 내에서 특정 CSS 선택자에 해당하는 요소들을 찾아 NodeList 형태로 반환하는 메소드이다.
            // NodeList - DOM에서 사용되는 객체로, 여러 개의 노드(HTML 요소, 속성, 텍스트 등)가 포함될 수 있는 리스트 형태의 객체를 나타낸다.
            //            일반적으로 querySelectorAll 메소드로 선택된 요소들을 반환하는데 사용된다.
            //            NodeList 객체는 배열과 유사하게 인덱스를 사용하여 접근할 수 있으며, length 속성을 통해 그 길이를 알 수 있다.
            //            하지만 NodeList는 배열이 아니므로, 배열 메소드를 사용할 수 없다.
            let audioTags = document.querySelectorAll("#chatArea audio");
            // forEach 메소드를 사용하여 audioTags 변수에 저장되어 있는 NodeList의 각 요소에 대하여 이벤트 리스너를 등록한다.
            audioTags.forEach(audio => {
                // forEach 메소드를 사용하여 audioTags 변수에 저장되어 있는 NodeList에서 가져온 각 audio(오디오) 태그마다,
                // "ended" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
                // ended - 오디오나 비디오 등의 미디어가 재생이 끝났을 때 발생하는 이벤트이다.
                audio.addEventListener("ended", () => {
                    // 각 audio(오디오) 태그의 재생이 종료되면, audio(오디오) 태그에 작성되어 있는 소스를 그대로 재작성한다.
                    // 이는 오디오 재생이 끝나고 다시 재생할 때, 오디오 소스가 초기화되도록 하는 역할을 한다.
                    // 이렇게 하는 이유는, 오디오 태그의 재생이 종료되고 나서 다시 오디오 태그가 재생될 때, 병합된 오디오 데이터를 온전히 다시 재생하기 위해서이다.
                    // 이렇게 하지 않으면, 오디오 태그의 재생이 종료되고 나서 다시 오디오 태그가 재생될 때, 병합된 오디오 데이터중 마지막 오디오 데이터만 재생된다.
                    // 하나의 오디오 데이터는 문제가 없지만, 병합된 여러개의 오디오 데이터는 이런 문제가 발생해 임시방편으로 찾아낸 방법이다.
                    audio.src = audio.src;
                }); // addEventListener
            }); // forEach
        // 6-2. type 값이 "record"가 아닌 경우
        } else {
            // 7. JSON형식으로 넘어온 DTO를 JavaScript형식으로 변환한다.
            //    JSON.parse(변환 대상) - JSON 문자열을 JavaScript 값이나 객체로 변환한다.
            var content = JSON.parse(chat.body);
            // 7-1. 변환된 DTO를 사용하기 편하게 각각 변수에 나눠놓는다.
            var writer = content.writer; // 작성자 닉네임
            var message = content.message; // 메시지
            var participant = content.participant; // 참가자 닉네임
            var profileImage = content.profileImage; // 참가자 프로필 사진
            var recruitingPersonnel = content.metaRecruitingPersonnel; // 참여중인 인원
            let timePassed = end - start; // 처음 메시지 전송후 1초 이내 다음 메시지 전송까지의 경과 시간 - 1초 이후 시작 시간 초기화
            let master = content.master; // 방장 닉네임
            let masterProfileImage = content.masterProfileImage; // 방장 프로필 사진
            let metaType = content.type; // 메시지 타입
            ////////////////////////////////////////////////////////////////////////////////////////////////////
            // 8. 참여중인 인원이 바뀌는 경우
            if ( recruitingPersonnel != null ) {
                // 전역 변수에 선언되어 있는 참여중인 인원을 새로 받아온 참여중인 인원으로 변경한다.
                metaRecruitingPersonnel = recruitingPersonnel;

                // 현재 작성되어 있는 방 정보를 바뀐 인원대로 변경해줘야 하기에 먼저 이전 방 정보를 제거하고 다시 올린다.

                // id를 통해 방 제목 구역에 작성되어 있는 방 정보을 가져와 title 변수에 저장한다.
                var title = document.getElementById("title");
                // title에 저장된 방 정보 id를 사용하여 해당 방 정보를 제거한다.
                title.remove();

                // 0. id를 통해 방 정보가 작성될 구역을 가져와 titleArea 변수에 저장한다.
                var titleArea = document.getElementById("titleArea");

                // document.createElement()를 사용하여, 참가자 코드를 작성해 참가자 구역에 올린다.
                // document.createElement() - 지정한 태그 이름을 가진 HTML 요소를 생성하는 메소드이다.
                //                            이 메소드를 사용하면 동적으로 HTML 요소를 생성할 수 있다.

                // 1. 방 제목 b 태그를 생성한다.
                var titleBTag = document.createElement("b");
                // 1-1. 방 제목 b 태그에 id 속성을 추가한다.
                titleBTag.setAttribute("id", "title");
                // 1-2. 방 제목 b 태그에 작성할 방 정보를 TextNode로 생성한다.
                var titleBContent = document.createTextNode("방 번호 : " + metaIdx + "    |    (스터디) 방 제목 : " + metaTitle + " ( " + recruitingPersonnel + " / " + metaPersonnel + " )");
                // 1-3. 방 제목 b 태그에 TextNode로 생성한 방 정보를 전달한다.
                titleBTag.appendChild(titleBContent);

                // 2. 방 제목 구역에 방 제목 b 태그를 올린다.
                titleArea.appendChild(titleBTag);
            } // recruitingPersonnel
            ////////////////////////////////////////////////////////////////////////////////////////////////////
            // 9. 방에 들어온 참가자가 있는 경우 - 첫 입장 및 재입장(새로고침)
            if ( participant != null ) {
                // 참가자가 새로 들어온게 아닌 새로고침한 경우 - 현재 작성되어 있는 참가자랑 중복되지 않게 먼저 이전 참가자를 제거하고 다시 올린다.

                // 참가자 닉네임 id를 통해 참가자 구역에 작성되어 있는 참가자를 가져와 ptr 변수에 저장한다.
                var ptr = document.getElementById(participant);
                // 가져온 참가자가 존재하는 경우
                if ( ptr != null ) {
                    // ptr에 저장된 참가자 id를 사용하여 해당 참가자를 제거한다.
                    ptr.remove();
                }

                // 0. id를 통해 참가자가 작성될 구역을 가져와 profileArea 변수에 저장한다.
                var profileArea = document.getElementById("profileArea");

                // document.createElement()를 사용하여, 참가자 코드를 작성해 참가자 구역에 올린다.
                // document.createElement() - 지정한 태그 이름을 가진 HTML 요소를 생성하는 메소드이다.
                //                            이 메소드를 사용하면 동적으로 HTML 요소를 생성할 수 있다.

                // 1. 이미지 태그를 생성한다.
                var imgTag = document.createElement("img");
                // element.setAttribute("AttributeName", "AttributeValue") - 지정한 HTML 요소의 속성과 값을 설정하는 메소드이다.
                //                                                           setAttribute 메소드는 두 개의 인자를 받는데, 첫 번째 인자는 설정할 속성의 이름이고, 두 번째 인자는 설정할 속성의 값이다.
                //                                                           만약 설정하려는 속성이 이미 존재하다면 해당 속성의 값만 설정할 속성의 값으로 업데이트되고, 그렇지 않다면 설정할 속성의 이름과 값으로 새 속성이 추가된다.
                // 1-1. 이미지 태그에 style 속성 class를 추가한다.
                imgTag.setAttribute("class", "participantImg");
                // 1-2. 참가자 프로필 사진을 가져와 이미지 태그의 소스로 전달한다.
                imgTag.src = "/imagePath/" + profileImage;

                // 2. 닉네임 b 태그를 생성한다.
                var nicknameBTag = document.createElement("b");
                // 2-1. 닉네임 b 태그에 style 속성 class를 추가한다.
                nicknameBTag.setAttribute("class", "participantNickname");
                // document.createTextNode() - 지정한 텍스트를 포함하는 새로운 Text 노드를 생성하는 메소드이다.
                //                             이 메소드를 사용하여 동적으로 HTML 요소의 텍스트를 업데이트할 수 있다.
                //                             이 메소드는 document.createElement()와 함께 사용하여 동적으로 HTML 요소를 생성하는 데 사용된다.
                // 2-2. 닉네임 b 태그에 작성할 참가자 닉네임을 TextNode로 생성한다.
                var nicknameBContent = document.createTextNode(participant);
                // 2-3. 닉네임 b 태그에 TextNode로 생성한 참가자 닉네임을 전달한다.
                nicknameBTag.appendChild(nicknameBContent);

                // 3. 작성자가 방장인지 체크한다.
                // 3-1. 작성자(참가자)가 방장 닉네임과 같은 경우 - 방장 참가
                if ( writer == metaMaster ) {
                    // 3-1-1. 방장 b 태그를 생성한다.
                    var masterBTag = document.createElement("b");
                    // 3-1-1-1. 방장 b 태그에 style 속성 class를 추가한다.
                    masterBTag.setAttribute("class", "participantMaster");
                    // 3-1-1-2. 방장 b 태그에 작성할 내용을 TextNode로 생성한다. - 방장 표시
                    var masterBContent = document.createTextNode("방장");
                    // 3-1-1-3. 방장 b 태그에 TextNode로 생성한 내용을 전달한다.
                    masterBTag.appendChild(masterBContent);

                    // 3-1-2. 닉네임 구역 div 태그를 생성한다.
                    var innerDivTag = document.createElement("div");
                    // 3-1-2-1. 닉네임 구역 div 태그에 style 속성 class를 추가한다.
                    innerDivTag.setAttribute("class", "participantInfo");
                    // 3-1-2-2. 닉네임 구역 div 태그에 내부 닉네임 b 태그를 전달한다.
                    innerDivTag.appendChild(nicknameBTag);
                    // 3-1-2-3. 닉네임 구역 div 태그에 내부 방장 b 태그를 전달한다.
                    innerDivTag.appendChild(masterBTag);
                // 3-2. 작성자(참가자)가 방장 닉네임과 다를 경우 - 유저 참가
                } else {
                    // 3-2-1. 닉네임 구역 div 태그를 생성한다.
                    var innerDivTag = document.createElement("div");
                    // 3-2-1-1. 닉네임 구역 div 태그에 style 속성 class를 추가한다.
                    innerDivTag.setAttribute("class", "participantInfo");
                    // 3-2-1-2. 닉네임 구역 div 태그에 내부 닉네임 b 태그를 전달한다.
                    innerDivTag.appendChild(nicknameBTag);
                } // writer == metaMaster

                // 4. 참가자 div 태그를 생성한다.
                var divTag = document.createElement("div");
                // 4-1. 참가자 div 태그에 상세 프로필 클릭 이벤트 class와 style 속성 class를 추가한다.
                divTag.setAttribute("class", "participant participantDiv");
                // 4-2. 참가자 div 태그에 id 속성을 작성자(참가자) 닉네임 값으로 설정해 추가한다.
                divTag.setAttribute("id", writer);
                // 4-3. 참가자 div 태그에 이미지 태그를 전달한다.
                divTag.appendChild(imgTag);
                // 4-4. 참가자 div 태그에 닉네임 구역 div 태그를 전달한다.
                divTag.appendChild(innerDivTag);

                // 5. 참가자 구역에 참가자 div 태그를 올린다.
                profileArea.appendChild(divTag);
            } // participant
            ////////////////////////////////////////////////////////////////////////////////////////////////////
            // 10. 메시지 타입 값에 따라 나눈다.
            /////////////////////////////////////////////// 첫 입장 ///////////////////////////////////////////////
            // 10-1. 메시지 타입이 "enter"일 경우
            if ( metaType == "enter" ) {
                // 메시지 코드를 작성한다.
                str = '<div style="text-align: left; color: green;">';
                str += '<b>' + message + '</b>';
                str += '</div>'
                // 작성한 메시지 코드를 채팅 구역에 올린다.
                // insertAdjacentHTML( position, html ) - position에 따른 위치에 html 요소를 추가 한다.
                // position에는 총 4가지의 옵션이 있다.
                // 1. beforebegin : 타겟 요소 전(형제 요소)에 생성한다. - 시작 태그의 앞 (형제 요소)
                // 2. afterbegin : 타겟 요소 다음(자식 요소)에 생성한다. - 시작 태그의 뒤 (자식 요소)
                // 3. beforeend : 타겟 요소 '끝나는 태그' 바로 직전(자식 요소)에 요소를 생성한다. - 종료 태그 앞 (자식 요소)
                // 4. afterend : 타겟 요소의 '끝나는 태그' 바로 다음(형제 요소)에 요소를 생성한다. - 종료 태그 뒤 (형제 요소)
                chatArea.insertAdjacentHTML("beforeend", str);
                // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
                chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;
            } // enter
            //////////////////////////////////////// 재입장(새로고침) - 폐기 ////////////////////////////////////////
//            // 10-2. 메시지 타입이 "reEnter"일 경우
//            if ( metaType == "reEnter" ) {
//                // 전달 받은 메시지가 존재하는지 체크한다.
//                // 메시지가 존재하지 않는 경우
//                if ( message == null ) {
//                    // 아무 작업 없이 돌아간다.
//                // 메시지가 존재하는 경우
//                } else {
//                    // 메시지 코드를 작성한다.
//                    str = '<div style="text-align: left;">';
//                    str += '<b>' + message + '</b>';
//                    str += '</div>'
//                    // 작성한 메시지 코드를 채팅 구역에 올린다.
//                    // insertAdjacentHTML( position, html ) - position에 따른 위치에 html 요소를 추가 한다.
//                    // position에는 총 4가지의 옵션이 있다.
//                    // 1. beforebegin : 타겟 요소 전(형제 요소)에 생성한다. - 시작 태그의 앞 (형제 요소)
//                    // 2. afterbegin : 타겟 요소 다음(자식 요소)에 생성한다. - 시작 태그의 뒤 (자식 요소)
//                    // 3. beforeend : 타겟 요소 '끝나는 태그' 바로 직전(자식 요소)에 요소를 생성한다. - 종료 태그 앞 (자식 요소)
//                    // 4. afterend : 타겟 요소의 '끝나는 태그' 바로 다음(형제 요소)에 요소를 생성한다. - 종료 태그 뒤 (형제 요소)
//                    chatArea.insertAdjacentHTML("beforeend", str);
//                    // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
//                    chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;
//                } // null
//            } // reEnter
            ////////////////////////////////////// 재입장(새로고침) 에러 - 폐기 //////////////////////////////////////
//            // 10-3. 메시지 타입이 "reErr"일 경우
//            if ( metaType == "reErr" ) {
//                // 작성자 닉네임과 로그인 유저 닉네임이 같은 경우 (본인)
//                if ( writer === metaNickname ) {
//                    // 작성자가 에러로 인해 퇴장할 때는 경고창 없이 즉시 나가지도록 이벤트 핸들러를 비활성화한다.
//                    // beforeunload 이벤트 핸들러를 비활성화한다.
//                    window.removeEventListener("beforeunload", handleBeforeUnload);
//                    // onunload 이벤트 핸들러를 비활성화한다.
//                    document.body.removeAttribute("onunload");
//                    // 메타 메인 페이지 URL에 파라미터로 방 번호와 닉네임 그리고 에러 메시지를 가져가 알람으로 에러 메시지를 띄우고 퇴장시킨다.
//                    location.href = "/meta?idx=" + metaIdx + "&nickname=" + writer + "&errRoom=" + "알수없는 이유로 방에서 탈주되었습니다.";
//                } // writer === metaNickname
//            } // reErr
            //////////////////////////////////////////////// 채팅 ////////////////////////////////////////////////
            // 10-4. 메시지 타입이 "chat"일 경우
            if ( metaType == "chat" ) {
                // 메시지 코드를 작성한다.
                str = '<div style="text-align: left;">';
                str += '<b>' + writer + ' : ' + message + '</b>';
                str += '</div>'
                // 작성한 메시지 코드를 채팅 구역에 올린다.
                // insertAdjacentHTML( position, html ) - position에 따른 위치에 html 요소를 추가 한다.
                // position에는 총 4가지의 옵션이 있다.
                // 1. beforebegin : 타겟 요소 전(형제 요소)에 생성한다. - 시작 태그의 앞 (형제 요소)
                // 2. afterbegin : 타겟 요소 다음(자식 요소)에 생성한다. - 시작 태그의 뒤 (자식 요소)
                // 3. beforeend : 타겟 요소 '끝나는 태그' 바로 직전(자식 요소)에 요소를 생성한다. - 종료 태그 앞 (자식 요소)
                // 4. afterend : 타겟 요소의 '끝나는 태그' 바로 다음(형제 요소)에 요소를 생성한다. - 종료 태그 뒤 (형제 요소)
                chatArea.insertAdjacentHTML("beforeend", str);
                // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
                chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;
            } // chat
            ////////////////////////////////////////////// 방장 위임 //////////////////////////////////////////////
            // 10-5 메시지 타입이 "DelegateMaster"일 경우
            if ( metaType == "DelegateMaster" ) {
                // 메시지 코드를 작성한다.
                str = '<div style="text-align: left; color: orange;">';
                str += '<b>' + message + '</b>';
                str += '</div>'
                // 작성한 메시지 코드를 채팅 구역에 올린다.
                // insertAdjacentHTML( position, html ) - position에 따른 위치에 html 요소를 추가 한다.
                // position에는 총 4가지의 옵션이 있다.
                // 1. beforebegin : 타겟 요소 전(형제 요소)에 생성한다. - 시작 태그의 앞 (형제 요소)
                // 2. afterbegin : 타겟 요소 다음(자식 요소)에 생성한다. - 시작 태그의 뒤 (자식 요소)
                // 3. beforeend : 타겟 요소 '끝나는 태그' 바로 직전(자식 요소)에 요소를 생성한다. - 종료 태그 앞 (자식 요소)
                // 4. afterend : 타겟 요소의 '끝나는 태그' 바로 다음(형제 요소)에 요소를 생성한다. - 종료 태그 뒤 (형제 요소)
                chatArea.insertAdjacentHTML("beforeend", str);
                // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
                chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;

                // 전역 변수에 선언되어 있는 위임 전 방장 닉네임을 위임 후 방장 닉네임으로 변경한다.
                metaMaster = master;

                // 현재 작성되어 있는 위임 전 방장과 위임 후 방장을 제거하고 다시 올린다.

                // 작성자(위임 전 방장) 닉네임 id를 통해 참가자 구역에 작성되어 있는 작성자(위임 전 방장)를 가져와 beforeMaster 변수에 저장한다.
                var beforeMaster = document.getElementById(writer);
                // 방장(위임 후 방장) 닉네임 id를 통해 참가자 구역에 작성되어 있는 방장(위임 후 방장)을 가져와 afterMaster 변수에 저장한다.
                var afterMaster = document.getElementById(master);
                // beforeMaster에 저장된 작성자(위임 전 방장) id를 사용하여 해당 참가자를 제거한다.
                beforeMaster.remove();
                // afterMaster에 저장된 방장(위임 후 방장) id를 사용하여 해당 참가자를 제거한다.
                afterMaster.remove();

                // id를 통해 작성자(위임 전 방장)와 방장(위임 후 방장)이 작성될 구역을 가져와 profileArea 변수에 저장한다.
                var profileArea = document.getElementById("profileArea");

                // 작성자(위임 전 방장) 코드를 작성한다.
                var beforeMasterCode = '<div class="participant participantDiv" id="' + writer + '">';
                beforeMasterCode += '<img class="participantImg" src="/imagePath/' + profileImage + '">';
                beforeMasterCode += '<div class="participantInfo">';
                beforeMasterCode += '<b class="participantNickname">' + writer + '</b>';
                beforeMasterCode += '</div>';
                beforeMasterCode += '</div>';
                // 작성한 작성자(위임 전 방장) 코드를 참가자 구역에 올린다.
                // insertAdjacentHTML( position, html ) - position에 따른 위치에 html 요소를 추가 한다.
                // position에는 총 4가지의 옵션이 있다.
                // 1. beforebegin : 타겟 요소 전(형제 요소)에 생성한다. - 시작 태그의 앞 (형제 요소)
                // 2. afterbegin : 타겟 요소 다음(자식 요소)에 생성한다. - 시작 태그의 뒤 (자식 요소)
                // 3. beforeend : 타겟 요소 '끝나는 태그' 바로 직전(자식 요소)에 요소를 생성한다. - 종료 태그 앞 (자식 요소)
                // 4. afterend : 타겟 요소의 '끝나는 태그' 바로 다음(형제 요소)에 요소를 생성한다. - 종료 태그 뒤 (형제 요소)
                profileArea.insertAdjacentHTML("beforeend", beforeMasterCode);

                // 방장(위임 후 방장) 코드를 작성한다.
                var afterMasterCode = '<div class="participant participantDiv" id="' + master + '">';
                afterMasterCode += '<img class="participantImg" src="' + masterProfileImage + '">';
                afterMasterCode += '<div class="participantInfo">';
                afterMasterCode += '<b class="participantNickname">' + master + '</b>';
                afterMasterCode += '<b class="participantMaster">' + '방장' + '</b>';
                afterMasterCode += '</div>';
                afterMasterCode += '</div>';
                // 작성한 방장(위임 후 방장) 코드를 참가자 구역에 올린다.
                // insertAdjacentHTML( position, html ) - position에 따른 위치에 html 요소를 추가 한다.
                // position에는 총 4가지의 옵션이 있다.
                // 1. beforebegin : 타겟 요소 전(형제 요소)에 생성한다. - 시작 태그의 앞 (형제 요소)
                // 2. afterbegin : 타겟 요소 다음(자식 요소)에 생성한다. - 시작 태그의 뒤 (자식 요소)
                // 3. beforeend : 타겟 요소 '끝나는 태그' 바로 직전(자식 요소)에 요소를 생성한다. - 종료 태그 앞 (자식 요소)
                // 4. afterend : 타겟 요소의 '끝나는 태그' 바로 다음(형제 요소)에 요소를 생성한다. - 종료 태그 뒤 (형제 요소)
                profileArea.insertAdjacentHTML("beforeend", afterMasterCode);
            } // DelegateMaster
            ////////////////////////////////////////////// 유저 강퇴 //////////////////////////////////////////////
            // 10-6. 메시지 타입이 "KickOut"일 경우
            if ( metaType == "KickOut" ) {
                // 메시지 코드를 작성한다.
                str = '<div style="text-align: left; color: purple;">';
                str += '<b>' + message + '</b>';
                str += '</div>'
                // 작성한 메시지 코드를 채팅 구역에 올린다.
                // insertAdjacentHTML( position, html ) - position에 따른 위치에 html 요소를 추가 한다.
                // position에는 총 4가지의 옵션이 있다.
                // 1. beforebegin : 타겟 요소 전(형제 요소)에 생성한다. - 시작 태그의 앞 (형제 요소)
                // 2. afterbegin : 타겟 요소 다음(자식 요소)에 생성한다. - 시작 태그의 뒤 (자식 요소)
                // 3. beforeend : 타겟 요소 '끝나는 태그' 바로 직전(자식 요소)에 요소를 생성한다. - 종료 태그 앞 (자식 요소)
                // 4. afterend : 타겟 요소의 '끝나는 태그' 바로 다음(형제 요소)에 요소를 생성한다. - 종료 태그 뒤 (형제 요소)
                chatArea.insertAdjacentHTML("beforeend", str);
                // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
                chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;

                // 작성자(강퇴자) 닉네임 id를 통해 참가자 구역에 작성되어 있는 작성자(강퇴자)를 가져와 ptr 변수에 저장한다.
                var ptr = document.getElementById(writer);
                // ptr에 저장된 작성자(강퇴자) id를 사용하여 해당 참가자를 제거한다.
                ptr.remove();

                // 작성자 닉네임과 로그인 유저 닉네임이 같은 경우 (본인)
                if ( writer === metaNickname ) {
                    alert(message);
                    // 작성자가 강퇴 당할 때는 경고창 없이 즉시 나가지도록 이벤트 핸들러를 비활성화한다.
                    // beforeunload 이벤트 핸들러를 비활성화한다.
                    window.removeEventListener("beforeunload", handleBeforeUnload);
                    // onunload 이벤트 핸들러를 비활성화한다.
                    document.body.removeAttribute("onunload");
                    // 방 퇴장 URL에 파라미터로 방 번호와 닉네임을 가져가 방에서 퇴장한다.
                    location.href = "/meta/selfexit?idx=" + metaIdx + "&nickname=" + writer;
                } // metaNickname
            } // KickOut
            ////////////////////////////////////////////// 방장 퇴장 //////////////////////////////////////////////
            // 10-7. 메시지 타입이 "delete"일 경우
            if ( metaType == "delete" ) {
                // fetch를 사용하여 방 퇴장 URL에 파라미터로 방 번호와 작성자 닉네임을 가져가 퇴장 처리한다.
                // fetch(url, { options: value }) - 브라우저에서 제공하는 API 중 하나로, 네트워크 요청을 보내고 응답을 받아오는 메소드이다.
                //                       fetch() 메소드는 첫 번째 인자로 서버에 데이터를 요청할 URL을 전달받고, 두 번째 인자로 선택적으로 HTTP 요청과 관련된 다양한 옵션을 설정하는 객체를 전달받는다.
                //                       여기서 옵션 객체는 HTTP 요청에 대한 옵션을 설정할 수 있는 여러 가지 속성을 가지고 있는데,
                //                       예를 들어 method 속성을 이용하여 HTTP 요청 메소드(GET, POST, PUT, DELETE 등)를 지정할 수 있으며,
                //                       method 속성을 지정하지 않으면 기본적으로 HTTP 요청 메서드는 GET이 된다.
                //                       그러기에 두 번째 인자로 옵션 객체를 전달하지 않으면 기본적으로 HTTP 요청 메서드는 GET으로 지정된다.
                //                       또 다른 속성으로는 headers, mode, cache, credentials 등이 있다.
                //                       fetch() 메소드는 Promise 객체를 반환하며, 이를 통해 비동기적으로 서버로부터 데이터를 받아올 수 있다.
                //                       응답을 받아오는 데 성공하면 Response 객체를 반환하고, 실패하면 Error 객체를 반환한다.
                // 예시 - 아래와 같이 fetch 메소드를 사용하여 JSON 데이터를 요청할 수 있다.
                // fetch("/meta/exit?idx=" + metaIdx + "&nickname=" + writer)
                //     .then(response => response.json()) // JSON 형식으로 파싱
                //     .then(data => { console.log(data) }) // 응답 데이터 처리
                //     .catch(error => console.error(error)); // 오류 처리
                // fetch 메소드는 Promise를 반환하기 때문에, then 메서드를 사용하여 비동기 처리를 할 수 있다.
                // fetch 메소드를 사용하여 "/meta/exit?idx=방 번호&nickname=작성자" 주소로 GET 요청을 보내고,
                // then() 메소드를 사용하여 요청이 성공했을 때 반환된 Response 객체를 json() 메소드를 통해 JSON 형식으로 변환한다.
                // 그리고 다시 then() 메소드를 사용하여 변환된 JSON 데이터를 콘솔에 출력한다.
                // 만약 요청이 실패하면 catch 메소드에서 에러를 출력한다.
                fetch("/meta/exit?idx=" + metaIdx + "&nickname=" + writer);
                // 여기서는 fetch를 사용하여 서버에서 퇴장 유저를 처리한 뒤, 클라이언트에서는 추가로 더 할 일이 없기에 then() 메소드를 사용하지 않는다.

                // 메시지 코드를 작성한다.
                str = '<div style="text-align: left; color: red;">';
                str += '<b>' + '방장이 탈주하여 1분뒤 방이 터집니다' + '</b>';
                str += '</div>'
                // 작성한 메시지 코드를 채팅 구역에 올린다.
                // insertAdjacentHTML( position, html ) - position에 따른 위치에 html 요소를 추가 한다.
                // position에는 총 4가지의 옵션이 있다.
                // 1. beforebegin : 타겟 요소 전(형제 요소)에 생성한다. - 시작 태그의 앞 (형제 요소)
                // 2. afterbegin : 타겟 요소 다음(자식 요소)에 생성한다. - 시작 태그의 뒤 (자식 요소)
                // 3. beforeend : 타겟 요소 '끝나는 태그' 바로 직전(자식 요소)에 요소를 생성한다. - 종료 태그 앞 (자식 요소)
                // 4. afterend : 타겟 요소의 '끝나는 태그' 바로 다음(형제 요소)에 요소를 생성한다. - 종료 태그 뒤 (형제 요소)
                chatArea.insertAdjacentHTML("beforeend", str);
                // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
                chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;

                // id를 통해 참가자 구역에서 방장을 가져온다.
                var mtr = document.getElementById(writer);
                // 가져온 방장 id를 이용하여 방장을 참가자 구역에서 제거한다.
                mtr.remove();

                // 방장 퇴장 후 방 삭제 메시지를 채팅 구역에 올리고, 방장을 참가자 구역에서 제거한 뒤,
                // setTimeout 메소드를 사용하여 방장이 퇴장한지 1분이 경과하면 방이 삭제되도록 한다.
                // 그리고 boomTimeoutId 변수에 setTimeout 메소드로 반환된 타이머 ID를 저장하여,
                // 1분이 경과되기 전에 방장이 재입장하면 boomTimeoutId로 타이머를 취소해 방이 삭제되지 안되도록 한다.
                // 이는 방장이 퇴장 후 1분이내 재입장 할 경우를 대비해 1분 대기하는 것이다.
                boomTimeoutId = setTimeout(function() {
                    // 방이 삭제될 때는 경고창 없이 즉시 나가지도록 이벤트 핸들러를 비활성화한다.
                    // beforeunload 이벤트 핸들러를 비활성화한다.
                    window.removeEventListener("beforeunload", handleBeforeUnload);
                    // onunload 이벤트 핸들러를 비활성화한다.
                    document.body.removeAttribute("onunload");
                    // 방 삭제 URL에 파라미터로 방 번호와 닉네임을 가져가 방 삭제 후 바로 퇴장한다.
                    location.href = "/meta/delete?idx=" + metaIdx + "&nickname=" + metaNickname;
                }, 60000); // setTimeout
            } // delete
            ////////////////////////////////////// 방장 퇴장 후 1분 이내 재입장 //////////////////////////////////////
            // 10-8. 메시지 타입이 "cancel"일 경우
            if ( metaType == "cancel" ) {
                // boomTimeoutId에 저장된 setTimeout ID를 사용하여 실행 중인 타이머를 취소한다.
                clearTimeout(boomTimeoutId);

                // 메시지 코드를 작성한다.
                str = '<div style="text-align: left; color: blue;">';
                str += '<b>' + '방장이 다시 입장하여 방이 유지됩니다.' + '</b>';
                str += '</div>'
                // 작성한 메시지 코드를 채팅 구역에 올린다.
                // insertAdjacentHTML( position, html ) - position에 따른 위치에 html 요소를 추가 한다.
                // position에는 총 4가지의 옵션이 있다.
                // 1. beforebegin : 타겟 요소 전(형제 요소)에 생성한다. - 시작 태그의 앞 (형제 요소)
                // 2. afterbegin : 타겟 요소 다음(자식 요소)에 생성한다. - 시작 태그의 뒤 (자식 요소)
                // 3. beforeend : 타겟 요소 '끝나는 태그' 바로 직전(자식 요소)에 요소를 생성한다. - 종료 태그 앞 (자식 요소)
                // 4. afterend : 타겟 요소의 '끝나는 태그' 바로 다음(형제 요소)에 요소를 생성한다. - 종료 태그 뒤 (형제 요소)
                chatArea.insertAdjacentHTML("beforeend", str);
                // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
                chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;
            } // cancel
            ////////////////////////////////////////////// 유저 퇴장 //////////////////////////////////////////////
            // 10-9. 메시지 타입이 "exit"일 경우
            if ( metaType == "exit" ) {
                // 작성자 닉네임과 로그인 유저 닉네임이 같은 경우 (본인)
                if ( writer === metaNickname ) {
                    // 작성자가 에러로 인해 퇴장할 때는 경고창 없이 즉시 나가지도록 이벤트 핸들러를 비활성화한다.
                    // beforeunload 이벤트 핸들러를 비활성화한다.
                    window.removeEventListener("beforeunload", handleBeforeUnload);
                    // onunload 이벤트 핸들러를 비활성화한다.
                    document.body.removeAttribute("onunload");
                    // 메타 메인 페이지 URL에 파라미터로 방 번호와 닉네임 그리고 에러 메시지를 가져가 알람으로 에러 메시지를 띄우고 퇴장시킨다.
                    location.href = "/meta?idx=" + metaIdx + "&nickname=" + writer + "&errRoom=" + "알수없는 이유로 방에서 탈주되었습니다.";
                } // writer === metaNickname

                // 방장 닉네임과 로그인 유저 닉네임이 같은 경우 - 방장이 퇴장 유저를 처리한다.
                if ( metaMaster == metaNickname ) {
                    // fetch를 사용하여 방 퇴장 URL에 파라미터로 방 번호와 작성자 닉네임을 가져가 퇴장 처리한다.
                    // fetch(url, { options: value }) - 브라우저에서 제공하는 API 중 하나로, 네트워크 요청을 보내고 응답을 받아오는 메소드이다.
                    //                                  fetch() 메소드는 첫 번째 인자로 서버에 데이터를 요청할 URL을 전달받고, 두 번째 인자로 선택적으로 HTTP 요청과 관련된 다양한 옵션을 설정하는 객체를 전달받는다.
                    //                                  여기서 옵션 객체는 HTTP 요청에 대한 옵션을 설정할 수 있는 여러 가지 속성을 가지고 있는데,
                    //                                  예를 들어 method 속성을 이용하여 HTTP 요청 메소드(GET, POST, PUT, DELETE 등)를 지정할 수 있으며,
                    //                                  method 속성을 지정하지 않으면 기본적으로 HTTP 요청 메서드는 GET이 된다.
                    //                                  그러기에 두 번째 인자로 옵션 객체를 전달하지 않으면 기본적으로 HTTP 요청 메서드는 GET으로 지정된다.
                    //                                  또 다른 속성으로는 headers, mode, cache, credentials 등이 있다.
                    //                                  fetch() 메소드는 Promise 객체를 반환하며, 이를 통해 비동기적으로 서버로부터 데이터를 받아올 수 있다.
                    //                                  응답을 받아오는 데 성공하면 Response 객체를 반환하고, 실패하면 Error 객체를 반환한다.
                    // 예시 - 아래와 같이 fetch 메소드를 사용하여 JSON 데이터를 요청할 수 있다.
                    // fetch("/meta/exit?idx=" + metaIdx + "&nickname=" + writer)
                    //     .then(response => response.json()) // JSON 형식으로 파싱
                    //     .then(data => { console.log(data) }) // 응답 데이터 처리
                    //     .catch(error => console.error(error)); // 오류 처리
                    // fetch 메소드는 Promise를 반환하기 때문에, then 메서드를 사용하여 비동기 처리를 할 수 있다.
                    // fetch 메소드를 사용하여 "/meta/exit?idx=방 번호&nickname=작성자" 주소로 GET 요청을 보내고,
                    // then() 메소드를 사용하여 요청이 성공했을 때 반환된 Response 객체를 json() 메소드를 통해 JSON 형식으로 변환한다.
                    // 그리고 다시 then() 메소드를 사용하여 변환된 JSON 데이터를 콘솔에 출력한다.
                    // 만약 요청이 실패하면 catch 메소드에서 에러를 출력한다.
                    fetch("/meta/exit?idx=" + metaIdx + "&nickname=" + writer);
                    // 여기서는 fetch를 사용하여 서버에서 퇴장 유저를 처리한 뒤, 클라이언트에서는 추가로 더 할 일이 없기에 then() 메소드를 사용하지 않는다.
                } // metaMaster

                // 메시지 코드를 작성한다.
                str = '<div style="text-align: left; color: purple;">';
                str += '<b>' + message + '</b>';
                str += '</div>'
                // 작성한 메시지 코드를 채팅 구역에 올린다.
                // insertAdjacentHTML( position, html ) - position에 따른 위치에 html 요소를 추가 한다.
                // position에는 총 4가지의 옵션이 있다.
                // 1. beforebegin : 타겟 요소 전(형제 요소)에 생성한다. - 시작 태그의 앞 (형제 요소)
                // 2. afterbegin : 타겟 요소 다음(자식 요소)에 생성한다. - 시작 태그의 뒤 (자식 요소)
                // 3. beforeend : 타겟 요소 '끝나는 태그' 바로 직전(자식 요소)에 요소를 생성한다. - 종료 태그 앞 (자식 요소)
                // 4. afterend : 타겟 요소의 '끝나는 태그' 바로 다음(형제 요소)에 요소를 생성한다. - 종료 태그 뒤 (형제 요소)
                chatArea.insertAdjacentHTML("beforeend", str);
                // 메시지가 구역을 넘어간다면 해당 구역에 스크롤이 생성되는데 스크롤을 언제나 가장 아래에 위치하게 만든다.
                chatArea.scrollTop = chatArea.scrollHeight - chatArea.clientHeight;

                // 작성자(퇴장자) 닉네임의 id를 통해 참가자 구역에 작성되어 있는 작성자(퇴장자)를 가져와 mtr 변수에 저장한다.
                var mtr = document.getElementById(writer);
                // mtr에 저장된 작성자(퇴장자) id를 사용하여 해당 참가자를 제거한다.
                mtr.remove();
            } // exit
            ///////////////////////////////// 참가자를 클릭할 경우 상세 프로필 구역 등장 /////////////////////////////////
            // 11. 상세 프로필 기능
            // 참가자 구역에 올라가 있는 "participant" class에 해당하는 태그를 모두 선택하여 participants 변수에 NodeList 형태로 저장한다.
            // querySelectorAll - HTML 문서 내에서 특정 CSS 선택자에 해당하는 요소들을 찾아 NodeList 형태로 반환하는 메소드이다.
            // NodeList - DOM에서 사용되는 객체로, 여러 개의 노드(HTML 요소, 속성, 텍스트 등)가 포함될 수 있는 리스트 형태의 객체를 나타낸다.
            //            일반적으로 querySelectorAll 메소드로 선택된 요소들을 반환하는데 사용된다.
            //            NodeList 객체는 배열과 유사하게 인덱스를 사용하여 접근할 수 있으며, length 속성을 통해 그 길이를 알 수 있다.
            //            하지만 NodeList는 배열이 아니므로, 배열 메소드를 사용할 수 없다.
            let participants = document.querySelectorAll(".participant");
            // forEach 메소드를 사용하여 participants 변수에 저장되어 있는 NodeList의 각 요소에 대하여 이벤트 리스너를 등록한다.
            participants.forEach(participant => {
                // forEach 메소드를 사용하여 participants 변수에 저장되어 있는 NodeList에서 가져온 각 "participant" class에 해당하는 태그마다,
                // "click" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
                // click - 마우스나 터치 디바이스의 클릭이나 탭을 할 때 발생하는 이벤트이다.
                participant.addEventListener("click", () => {
                    // id를 통해 상세 프로필이 작성될 구역을 가져와 detailProfileArea 변수에 저장한다.
                    var detailProfileArea = document.getElementById("detailProfileArea"); // 상세 프로필 창
                    // 상세 프로필 구역의 style에 "display"를 "block"으로 설정한다.
                    detailProfileArea.style.display = "block"; // 상세 프로필 창 열기
                    chatArea.style.height = "72.58%"; // 채팅창 높이 감소

                    // id를 통해 상세 프로필 구역에 올라가 있는 창 닫기 버튼을 가져와 btnDetailProfileClose 변수에 저장한다.
                    var btnDetailProfileClose = document.getElementById("btnDetailProfileClose"); // 상세 프로필 창 닫기 버튼

                    ////////////////////////////////////////////////////////////////////////////////////////////
                    // 참가자를 클릭하여 상세 프로필을 열람할 때는 항상 가장 먼저 이전 상세 프로필에서 생성된 "방장" 글과 버튼 구역이 계속 존재하는지부터 체크해, 계속 존재한다면 모두 제거한다.
                    // 이 작업을 안 거칠 경우 참가자를 클릭할 때마다 이전 상세 프로필에서 생성된 "방장" 글과 버튼 구역에 이어서 중복으로 계속 생성된다.

                    // 상세 프로필 구역에 올라가 있는 "detailProfileMaster" id에 해당하는 태그를 선택하여 detailProfileMaster 변수에 NodeList 형태로 저장한다.
                    var detailProfileMaster = detailProfileArea.querySelector("#detailProfileMaster"); // 상세 프로필에 작성되어 있는 "방장" 글
                    // detailProfileMaster에 저장한 값이 존재하는지 체크한다.
                    // detailProfileMaster에 값이 존재하는 경우
                    if ( detailProfileMaster != null ) {
                        // profileBtn에 저장된 상세 프로필 방장 id를 사용하여 해당 상세 프로필에 작성되어 있는 "방장" 글을 제거한다.
                        detailProfileMaster.remove();
                    }

                    // 상세 프로필 구역에 올라가 있는 "detailProfileBtnArea" id에 해당하는 태그를 선택하여 detailProfileBtnArea 변수에 NodeList 형태로 저장한다.
                    var detailProfileBtnArea = detailProfileArea.querySelector("#detailProfileBtnArea"); // 상세 프로필에 생성되어 있는 버튼 구역
                    // detailProfileBtnArea에 저장한 값이 존재하는지 체크한다.
                    // detailProfileBtnArea에 값이 존재하는 경우
                    if ( detailProfileBtnArea != null ) {
                        // profileBtn에 저장된 상세 프로필 버튼 구역 id를 사용하여 해당 상세 프로필에 생성되어 있는 버튼들을 제거한다.
                        detailProfileBtnArea.remove();
                    }
                    ////////////////////////////////////////////////////////////////////////////////////////////
                    // 클릭한 참가자의 id를 가져와 participantNickname 변수에 저장한다.
                    var participantNickname = participant.id; // 참가자 닉네임
                    // 클릭한 참가자의 프로필 구역에서 img(이미지) 태그를 선택하여 participantImage 변수에 프로필 사진 소스를 NodeList 형태로 저장한다.
                    var participantImage = participant.querySelector("img").src; // 참가자 프로필 사진

                    // id를 통해 상세 프로필 구역에 올라가 있는 프로필 사진을 가져와 detailProfileImage 변수에 저장한다.
                    var detailProfileImage = document.getElementById("detailProfileImage");
                    // detailProfileImage에 소스를 클릭한 참가자의 프로필 사진 소스로 설정한다.
                    detailProfileImage.setAttribute("src", participantImage);

                    // id를 통해 상세 프로필 구역에 올라가 있는 프로필 닉네임을 가져와 detailProfileNickname 변수에 저장한다.
                    var detailProfileNickname = document.getElementById("detailProfileNickname");
                    // detailProfileNickname에 클릭한 참가자의 닉네임으로 작성한다.
                    detailProfileNickname.innerText = participantNickname;

                    // 참가자 닉네임과 방장 닉네임이 같은 경우 - 방장'을' 클릭
                    if ( participantNickname === metaMaster ) {
                        // 방장 b 태그를 생성한다.
                        var detailProfileMasterBTag = document.createElement("b");
                        // 방장 b 태그에 id 속성을 추가한다.
                        detailProfileMasterBTag.setAttribute("id", "detailProfileMaster");
                        // 방장 b 태그에 style 속성을 추가한다.
                        detailProfileMasterBTag.setAttribute("style", "color: red;");
                        // document.createTextNode() - 지정한 텍스트를 포함하는 새로운 Text 노드를 생성하는 메소드이다.
                        //                             이 메소드를 사용하여 동적으로 HTML 요소의 텍스트를 업데이트할 수 있다.
                        //                             이 메소드는 document.createElement()와 함께 사용하여 동적으로 HTML 요소를 생성하는 데 사용된다.
                        // 방장 b 태그에 작성할 내용을 TextNode로 생성한다.
                        var detailProfileMasterInnerBContent = document.createTextNode("방장");
                        // 방장 b 태그에 TextNode로 생성한 내용을 전달한다.
                        detailProfileMasterBTag.appendChild(detailProfileMasterInnerBContent);

                        // id를 통해 상세 프로필 구역에 올라가 있는 프로필 닉네임 구역을 가져와 detailProfileNicknameArea 변수에 저장한다.
                        var detailProfileNicknameArea = document.getElementById("detailProfileNicknameArea");
                        // 상세 프로필 구역에 올라가 있는 프로필 닉네임 구역에 b 태그를 올린다.
                        detailProfileNicknameArea.appendChild(detailProfileMasterBTag);
                    } // participantNickname === metaMaster

                    // 로그인 유저 닉네임과 방장 닉네임이 같은 경우  - 방장'이' 클릭
                    if ( metaNickname === metaMaster ) {
                        // 로그인 유저 닉네임과 참가자 닉네임이 다른 경우 - 방장 본인 제외 타 유저 클릭
                        if ( metaNickname != participantNickname ) {
                            // 방장 위임 버튼 input 태그를 생성한다.
                            var detailProfileBtnInnerLeftInputTag = document.createElement("input");
                            // 방장 위임 버튼 input 태그에 id 속성을 추가한다.
                            detailProfileBtnInnerLeftInputTag.setAttribute("id", "btnDetailProfileDelegateMaster");
                            // 방장 위임 버튼 input 태그에 type 속성을 버튼으로 설정해 추가한다. - input 버튼 태그 생성
                            detailProfileBtnInnerLeftInputTag.setAttribute("type", "button");
                            // 방장 위임 버튼 input 태그에 value 속성으로 버튼 이름을 설정해 추가한다.
                            detailProfileBtnInnerLeftInputTag.setAttribute("value", "방장 위임");
                            // 방장 위임 버튼 input 태그에 style 속성을 추가한다.
                            detailProfileBtnInnerLeftInputTag.setAttribute("style", "width: 50%;");

                            // 왼쪽 div 태그를 생성한다.
                            var detailProfileBtnInnerLeftDivTag = document.createElement("div");
                            // 왼쪽 div 태그에 style 속성을 추가한다.
                            detailProfileBtnInnerLeftDivTag.setAttribute("style", "width: 50%; display: flex; justify-content: center; align-items: center;");
                            // 왼쪽 div 태그에 방장 위임 버튼 input 태그를 전달한다.
                            detailProfileBtnInnerLeftDivTag.appendChild(detailProfileBtnInnerLeftInputTag);

                            // 강퇴 버튼 input 태그를 생성한다.
                            var detailProfileBtnInnerRightInputTag = document.createElement("input");
                            // 강퇴 버튼 input 태그에 id 속성을 추가한다.
                            detailProfileBtnInnerRightInputTag.setAttribute("id", "btnDetailProfileKickOut");
                            // 강퇴 버튼 input 태그에 type 속성을 버튼으로 설정해 추가한다. - input 버튼 태그 생성
                            detailProfileBtnInnerRightInputTag.setAttribute("type", "button");
                            // 강퇴 버튼 input 태그에 value 속성으로 버튼 이름을 설정해 추가한다.
                            detailProfileBtnInnerRightInputTag.setAttribute("value", "강퇴");
                            // 강퇴 버튼 input 태그에 style 속성을 추가한다.
                            detailProfileBtnInnerRightInputTag.setAttribute("style", "width: 50%;");

                            // 오른쪽 div 태그를 생성한다.
                            var detailProfileBtnInnerRightDivTag = document.createElement("div");
                            // 오른쪽 div 태그에 style 속성을 추가한다.
                            detailProfileBtnInnerRightDivTag.setAttribute("style", "width: 50%; display: flex; justify-content: center; align-items: center;");
                            // 오른쪽 div 태그에 강퇴 버튼 input 태그를 전달한다.
                            detailProfileBtnInnerRightDivTag.appendChild(detailProfileBtnInnerRightInputTag);

                            // 상세 프로필 버튼 구역 div 태그를 생성한다.
                            var detailProfileBtnAreaDivTag = document.createElement("div");
                            // 상세 프로필 버튼 구역 div 태그에 id 속성을 추가한다.
                            detailProfileBtnAreaDivTag.setAttribute("id", "detailProfileBtnArea");
                            // 상세 프로필 버튼 구역 div 태그에 style 속성을 추가한다.
                            detailProfileBtnAreaDivTag.setAttribute("style", "display: flex; justify-content: space-between; height: 10%; width: 100%;");
                            // 상세 프로필 버튼 구역 div 태그에 왼쪽 div 태그를 전달한다.
                            detailProfileBtnAreaDivTag.appendChild(detailProfileBtnInnerLeftDivTag);
                            // 상세 프로필 버튼 구역 div 태그에 오른쪽 div 태그를 전달한다.
                            detailProfileBtnAreaDivTag.appendChild(detailProfileBtnInnerRightDivTag);

                            // 상세 프로필 구역에 상세 프로필 버튼 구역 div 태그를 올린다.
                            detailProfileArea.appendChild(detailProfileBtnAreaDivTag);
                            ////////////////////////////////////////////////////////////////////////////////////
                            // id를 통해 상세 프로필 버튼 구역에 올라가 있는 방장 위임 버튼을 가져와 detailProfileNicknameArea 변수에 저장한다.
                            var btnDetailProfileDelegateMaster = document.getElementById("btnDetailProfileDelegateMaster");
                            // 방장 위임 버튼에서 "click" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
                            // click - 마우스나 터치 디바이스의 클릭이나 탭을 할 때 발생하는 이벤트이다.
                            btnDetailProfileDelegateMaster.addEventListener("click", () => {
                                // fetch를 사용하여 방장 위임 URL에 파라미터로 방 번호와 방장을 위임할 참가자 닉네임을 가져가 방장을 위임한다.
                                // fetch(url, { options: value }) - 브라우저에서 제공하는 API 중 하나로, 네트워크 요청을 보내고 응답을 받아오는 메소드이다.
                                //                       fetch() 메소드는 첫 번째 인자로 서버에 데이터를 요청할 URL을 전달받고, 두 번째 인자로 선택적으로 HTTP 요청과 관련된 다양한 옵션을 설정하는 객체를 전달받는다.
                                //                       여기서 옵션 객체는 HTTP 요청에 대한 옵션을 설정할 수 있는 여러 가지 속성을 가지고 있는데,
                                //                       예를 들어 method 속성을 이용하여 HTTP 요청 메소드(GET, POST, PUT, DELETE 등)를 지정할 수 있으며,
                                //                       method 속성을 지정하지 않으면 기본적으로 HTTP 요청 메서드는 GET이 된다.
                                //                       그러기에 두 번째 인자로 옵션 객체를 전달하지 않으면 기본적으로 HTTP 요청 메서드는 GET으로 지정된다.
                                //                       또 다른 속성으로는 headers, mode, cache, credentials 등이 있다.
                                //                       fetch() 메소드는 Promise 객체를 반환하며, 이를 통해 비동기적으로 서버로부터 데이터를 받아올 수 있다.
                                //                       응답을 받아오는 데 성공하면 Response 객체를 반환하고, 실패하면 Error 객체를 반환한다.
                                // 예시 - 아래와 같이 fetch 메소드를 사용하여 JSON 데이터를 요청할 수 있다.
                                // fetch("/meta/delegatemaster?idx=" + metaIdx + "&nickname=" + participantNickname)
                                //     .then(response => response.json()) // JSON 형식으로 파싱
                                //     .then(data => { console.log(data) }) // 응답 데이터 처리
                                //     .catch(error => console.error(error)); // 오류 처리
                                // fetch 메소드는 Promise를 반환하기 때문에, then 메서드를 사용하여 비동기 처리를 할 수 있다.
                                // fetch 메소드를 사용하여 "/meta/delegatemaster?idx=방 번호&nickname=방장을 위임할 참가자 닉네임" 주소로 GET 요청을 보내고,
                                // then() 메소드를 사용하여 요청이 성공했을 때 반환된 Response 객체를 json() 메소드를 통해 JSON 형식으로 변환한다.
                                // 그리고 다시 then() 메소드를 사용하여 변환된 JSON 데이터를 콘솔에 출력한다.
                                // 만약 요청이 실패하면 catch 메소드에서 에러를 출력한다.
                                fetch("/meta/delegatemaster?idx=" + metaIdx + "&nickname=" + participantNickname)
                                    .then(response => response.json())
                                    .then(data => {
                                        // 방장 위임 URL에서 방장을 위임 처리하고 받아온 결과 값을 체크한다.
                                        // 1 - 성공 / 0 - 실패
                                        // 결과 값이 1인 경우 - 성공
                                        if ( data == 1 ) {
                                            // send(path, header, message)로 방장 위임 메시지를 전송한다. (방장 위임은 여기서 메시지를 전송한다.)
                                            // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
                                            // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 파라미터를 통해 받는다.
                                            stomp.send('/pub/meta/studyroom/delegatemaster', {}, JSON.stringify({type: "DelegateMaster", metaIdx: metaIdx, metaTitle: metaTitle, writer: metaMaster, profileImage: metaProfileImage, metaRecruitingPersonnel: metaRecruitingPersonnel, master: participantNickname, masterProfileImage: participantImage}));
                                            // 상세 프로필 구역의 style에 "display"를 "none"으로 설정한다.
                                            detailProfileArea.style.display = "none"; // 상세 프로필 창 닫기
                                        }
                                    })
                                    .catch(error => console.error(error)); // fetch
                            }); // addEventListener

                            // id를 통해 상세 프로필 버튼 구역에 올라가 있는 강퇴 버튼을 가져와 detailProfileNicknameArea 변수에 저장한다.
                            var btnDetailProfileKickOut = document.getElementById("#btnDetailProfileKickOut");
                            // 강퇴 버튼에서 "click" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
                            // click - 마우스나 터치 디바이스의 클릭이나 탭을 할 때 발생하는 이벤트이다.
                            btnDetailProfileKickOut.addEventListener("click", () => {
                                // send(path, header, message)로 강퇴 메시지를 전송한다. (모든 강퇴는 여기서 메시지를 전송한다.)
                                // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
                                // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 파라미터를 통해 받는다.
                                stomp.send('/pub/meta/studyroom/kickout', {}, JSON.stringify({type: "KickOut", metaIdx: metaIdx, metaTitle: metaTitle, writer: participantNickname, metaRecruitingPersonnel: metaRecruitingPersonnel}));
                                // 상세 프로필 구역의 style에 "display"를 "none"으로 설정한다.
                                detailProfileArea.style.display = "none"; // 상세 프로필 창 닫기
                            }); // addEventListener
                            ////////////////////////////////////////////////////////////////////////////////////
                        } // metaNickname != participantNickname
                    } // metaNickname === metaMaster

                    // 상세 프로필 창 닫기 버튼에서 "click" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
                    // click - 마우스나 터치 디바이스의 클릭이나 탭을 할 때 발생하는 이벤트이다.
                    btnDetailProfileClose.addEventListener("click", () => {
                        // 상세 프로필 구역의 style에 "display"를 "none"으로 설정한다.
                        detailProfileArea.style.display = "none"; // 상세 프로필 창 닫기
                        chatArea.style.height = "98.7%"; // 채팅창 높이 복구
                    }); // addEventListener
                }); // addEventListener
            }); // forEach
        } // record
    }); // stomp
    ////////////////////////////////////////////////// 입장 구역 //////////////////////////////////////////////////
    // 입장 시작!! - 먼저 입장 체크값을 이용하여 해당 유저가 첫 입장인지 재입장(새로고침)인지 체크한다.
    // 첫 입장일 경우 - 입장 체크값이 존재하지 않는다.
    if ( entryCheck == null ) {
        // 이전 로컬 스토리지 값들을 제거한다.
        localStorage.clear();
        // 시작 시간과 메시지 전송 시간과 전송된 메시지 수와 메시지 전송 상태의 각 변수명을 키로 사용하고, 각 초기값을 지정해 값으로 사용하여 로컬 스토리지에 추가한다.
        localStorage.setItem("start", Date.now()); // Date.now()로 초기화
        localStorage.setItem("end", Date.now()); // Date.now()로 초기화
        localStorage.setItem("count", 0); // 0으로 초기화
        localStorage.setItem("sendMessage", "true"); // true로 초기화
        // 로컬 스토리지에 추가한 값들을 가져와 각 알맞는 변수에 전달한다.
        start = localStorage.getItem("start");
        end = localStorage.getItem("end");
        count = localStorage.getItem("count");
        sendMessage = localStorage.getItem("sendMessage");
        // 4번이 5번보다 아래에 위치한 이유 - 위에 있을경우 간혹 4번에서 전송한 메시지를 제대로 전달받지 못하는 경우가 존재한다.
        // 4. send(path, header, message)로 입장 메시지를 전송한다. (첫 입장할때 딱 한번만 여기서 입장 메시지를 전송한다.)
        //    JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
        //    여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
        stomp.send('/pub/meta/studyroom/enter', {}, JSON.stringify({type: "enter", metaIdx: metaIdx, metaTitle: metaTitle, writer: metaNickname, profileImage: metaProfileImage, metaRecruitingPersonnel: metaRecruitingPersonnel}));
    // 재입장(새로고침)일 경우 - 입장 체크값이 존재한다.
    } else {
        // 4번이 5번보다 아래에 위치한 이유 - 위에 있을경우 간혹 4번에서 전송한 메시지를 제대로 전달받지 못하는 경우가 존재한다.
        // 4. send(path, header, message)로 입장 메시지를 전송한다. (첫 입장 이후 모든 재입장(새로고침)은 여기서 입장 메시지를 전송한다.)
        //    JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
        //    여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
        stomp.send('/pub/meta/studyroom/reenter', {}, JSON.stringify({type: "reEnter", metaIdx: metaIdx, writer: metaNickname, profileImage: metaProfileImage, metaRecruitingPersonnel: metaRecruitingPersonnel}));
    }
});
//////////////////////////////////////////////////// 채팅 구역 ////////////////////////////////////////////////////
// input 텍스트 태그에서 "keypress" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// keypress - 웹 페이지에서 키보드를 눌렀을 때 발생한다.
message.addEventListener("keypress", function(event) {
    // 눌린 키가 무엇인지 체크한다.
    // 눌린 키가 엔터 키인 경우
    if ( event.key === "Enter" ) {
        // 채팅 메시지 전송 메소드로 이동한다.
        send();
    }
});

// 채팅 메시지 전송 메소드
function send() {
    // id를 통해 작성한 메시지를 가져온다.
    let message = document.getElementById("message");
    // 가져온 메시지의 값을 체크한다.
    // 가져온 메시지의 값이 비어있을 경우
    if ( message.value == "" ) {
        // 먼저 경고 알림창을 띄워준다.
        alert("메시지를 작성해주세요.");
        // 그 다음 아무 작업 없이 돌아간다.
        return;
    // 가져온 메시지의 값이 비어있지 않을 경우
    } else {
        // 1초 이내 5개 이상의 메시지를 보낼 경우 - 5초간 메시지를 보낼 수 없다.
        // ( end - start ) - 처음 메시지 전송후 1초 이내 다음 메시지 전송까지의 경과 시간
        if ( ( end - start ) <= 1000 && count >= 5 ) {
            alert("짧은 시간에 많은 메시지를 보낼 수 없습니다. 잠시 후 다시 시도해주세요.");
            // 먼저 로컬 스토리지에서 현재 가지고 있는 메시지 전송 상태를 제거한다.
            localStorage.removeItem("sendMessage");
            // 그 다음 메시지 전송 상태 값을 메시지를 보낼 수 없는 상태로 변경하여 다시 로컬 스토리지에 추가한다.
            localStorage.setItem("sendMessage", "false");
            // 마지막으로 로컬 스토리지에 다시 추가한 값을 가져와 알맞는 변수에 전달한다.
            sendMessage = localStorage.getItem("sendMessage");
            // 5초 대기 후 내부 메소드를 실행한다.
            setTimeout(function() {
                // 먼저 로컬 스토리지에서 현재 가지고 있는 시작 시간과 전송된 메시지 수와 메시지 전송 상태를 제거한다.
                localStorage.removeItem("start");
                localStorage.removeItem("count");
                localStorage.removeItem("sendMessage");
                // 그 다음 시작 시간과 전송된 메시지 수와 메시지 전송 상태 값을 다시 초기값으로 변경하여 로컬 스토리지에 추가한다.
                localStorage.setItem("start", Date.now());
                localStorage.setItem("count", 0);
                localStorage.setItem("sendMessage", "true");
                // 마지막으로 로컬 스토리지에 다시 추가한 값들을 가져와 각 알맞는 변수에 전달한다.
                start = localStorage.getItem("start");
                count = localStorage.getItem("count");
                sendMessage = localStorage.getItem("sendMessage");
            }, 5000);
        // 그 외 경우
        } else {
            // 메시지를 보낼 수 없는 상태인 경우 - 아직 5초 대기중이다.
            if ( sendMessage == "false" ) {
                // 받아온 메시지를 올리지 않고 통과
                return;
            // 메시지를 보낼 수 있는 상태인 경우
            } else {
                // 먼저 로컬 스토리지에서 현재 가지고 있는 메시지 전송 시간을 제거한다.
                localStorage.removeItem("end");
                // 그 다음 메시지 전송 시간 값을 다시 초기값으로 변경하여 로컬 스토리지에 추가한다.
                localStorage.setItem("end", Date.now());
                // 마지막으로 로컬 스토리지에 다시 추가한 값을 가져와 알맞는 변수에 전달한다.
                end = localStorage.getItem("end");
                // 메시지를 전송한지 1초가 지날 경우 - 다시 초기 상태로 돌아간다.
                if ( ( end - start ) > 1000 ) {
                    // 먼저 로컬 스토리지에서 현재 가지고 있는 시작 시간과 전송된 메시지 수를 제거한다.
                    localStorage.removeItem("start");
                    localStorage.removeItem("count");
                    // 그 다음 시작 시간과 전송된 메시지 수의 값을 다시 초기값으로 변경하여 로컬 스토리지에 추가한다.
                    localStorage.setItem("start", Date.now());
                    localStorage.setItem("count", 0);
                    // 마지막으로 로컬 스토리지에 다시 추가한 값들을 가져와 각 알맞는 변수에 전달한다.
                    start = localStorage.getItem("start");
                    count = localStorage.getItem("count");
                }
                // send(path, header, message)로 채팅 메시지를 전송한다. (입장 이후 작성되는 모든 메시지는 여기서 전송한다.)
                // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
                // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
                stomp.send('/pub/meta/studyroom/message', {}, JSON.stringify({type: "chat", metaIdx: metaIdx, message: message.value, writer: metaNickname}));
                message.value = ""; // 메시지를 전송한 뒤 공백 상태로 만든다.
                // 먼저 전송된 메시지 수를 1 증가시킨다.
                count++;
                // 그 다음 로컬 스토리지에서 현재 가지고 있는 전송된 메시지 수를 제거한다.
                localStorage.removeItem("count");
                // 그 다음 메시지 수의 값을 증가한 값으로 변경하여 다시 로컬 스토리지에 추가한다.
                localStorage.setItem("count", count);
                // 마지막으로 로컬 스토리지에 다시 추가한 값을 가져와 알맞는 변수에 전달한다.
                count = localStorage.getItem("count");
            }
        }
    }
}
////////////////////////////////////////////////// 녹음 변수 구역 //////////////////////////////////////////////////
let record = document.getElementById("record"); // 오디오 태그
let btnRecordStart = document.getElementById("btnRecordStart"); // 녹음 시작 버튼
let btnRecordStop = document.getElementById("btnRecordStop"); // 녹음 정지 버튼
let btnRecordSend = document.getElementById("btnRecordSend"); // 녹음 전송 버튼
let mediaRecorder; // MediaRecorder 객체
let recordedChunks = []; // blob 녹음 데이터
let recordTimeoutId; // 녹음 setTimeout ID
let recordBlob; // 병합한 Blob 녹음 데이터
let recordUrl; // 병합한 Blob 녹음 데이터의 URL
//////////////////////////////////////////////////// 녹음 구역 ////////////////////////////////////////////////////
// 녹음 시작 버튼 클릭 시 호출되는 메소드
async function startRecording() {
    // navigator.mediaDevices.getUserMedia() - 미디어 스트림(비디오 또는 오디오)을 생성하는 메소드로 비동기 메소드이며, 사용자의 미디어 장치(예: 마이크)를 사용할 수 있는 권한이 있는지를 확인하고, 권한이 있다면 미디어 스트림을 반한다.
    // getUserMedia() 메소드는 mediaDevices 객체에 호출되며, 사용자에게 미디어 장치 사용 권한을 요청한다.
    // getUserMedia()에 전달된 매개변수는 'audio' 속성을 true로 설정하여 사용자의 오디오 장치에 액세스하려는 것을 지정한다.
    // getUserMedia()는 Promise 객체를 반환하며,
    // 요청한 미디어 타입에 대한 스트림을 성공적으로 가져올 경우 then() 메소드를 호출하여 성공적인 MediaStream 객체를 반환한다.
    // 반면, 접근 권한이 거부되거나 미디어 장치가 없는 경우 catch() 메소드를 호출하여 NotAllowedError 또는 NotFoundError 오류를 반환한다.
    // getUserMedia()는 안전한 출처 (즉, localhost, HTTPS)에서만 작동한다.
    navigator.mediaDevices.getUserMedia({ audio: true })
        // then 메소드는 Promise 객체가 resolve된 후 호출된다.
        .then(stream => {
            try {
                // 브라우저가 WAV 오디오 형식을 지원하는지 검사한다.
                // WAV 오디오 형식을 지원하는 경우
                if ( typeof MediaRecorder.isTypeSupported == "function" && MediaRecorder.isTypeSupported( "audio/wav" ) ) {
                    // MediaRecorder 생성자를 사용하여 입력 스트림(stream)에서 오디오 데이터를 캡처하고,
                    // 해당 데이터의 MIME 타입을 "audio/wav"로 설정하는 MediaRecorder 객체를 생성해 mediaRecorder 변수에 전달한다.
                    // stream(입력 스트림) - getUserMedia() 메소드로 얻은 스트림을 저장하는 변수이며, 이를 MediaRecorder 생성자에 전달하여 오디오 데이터를 캡처한다.
                    mediaRecorder = new MediaRecorder(stream, { mimeType: "audio/wav" });
                // WAV 오디오 형식을 지원하지 않는 경우
                } else {
                    // MediaRecorder 생성자를 사용하여 입력 스트림(stream)에서 오디오 데이터를 캡처하고,
                    // 해당 데이터의 MIME 타입을 "audio/webm"으로 설정하는 MediaRecorder 객체를 생성해 mediaRecorder 변수에 전달한다.
                    // stream(입력 스트림) - getUserMedia() 메소드로 얻은 스트림을 저장하는 변수이며, 이를 MediaRecorder 생성자에 전달하여 오디오 데이터를 캡처한다.
                    mediaRecorder = new MediaRecorder(stream, { mimeType: "audio/webm" });
                }

                // MediaRecorder 객체에서 "dataavailable" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
                // dataavailable - MediaRecorder 객체에서 발생하는 이벤트 중 하나로 MediaRecorder가 녹음을 진행하면서 데이터를 누적할 때마다 발생하는 이벤트이다.
                //                 즉, 녹음된 오디오 데이터가 사용 가능해질 때마다 이 이벤트가 발생한다.
                //                 이 때 발생한 녹음된 오디오 데이터는 Blob 형태로 event.data에 담긴다.
                //                 그러기에 이 이벤트를 이용하면 MediaRecorder가 녹음한 오디오 데이터를 직접 다룰 수 있게 된다.
                mediaRecorder.addEventListener("dataavailable", function(event) {
                    // event.data에 들어있는 녹음된 오디오 데이터가 유효한지 체크한다.
                    // 녹음된 오디오 데이터가 유효하지 않은 경우
                    if (typeof event.data === "undefined") {
                        // 아무 작업 없이 돌아간다.
                        return;
                    // 녹음된 오디오 데이터가 유효한 경우
                    } else {
                        // 녹음된 오디오 데이터를 위에서 미리 만들어둔 recordedChunks 배열에 추가한다.
                        recordedChunks.push(event.data);
                        // Blob 생성자를 사용하여 recordedChunks 배열에 저장된 Blob 오디오 데이터들을 모두 가져와,
                        // MIME 타입을 "audio/webm"으로 설정하는 하나의 Blob 객체로 재생성해,
                        // recordBlob 변수에 전달한다.
                        recordBlob = new Blob(recordedChunks, { type: "audio/webm" });
                        // createObjectURL 메소드를 사용하여 재생성된 Blob 객체를 URL 객체로 변환해,
                        // recordUrl 변수에 전달한다.
                        recordUrl = URL.createObjectURL(recordBlob);
                        // .src를 사용하여 변환된 URL을 id값으로 가져온 현재 작성되어 있는 오디오 태그의 소스로 설정해,
                        // 녹음된 오디오를 전송하기 전에 녹음 내용을 들어 볼 수 있게 한다.
                        record.src = recordUrl;
                    }
                });

                // MediaRecorder의 start() 메소드를 호출하여 녹음을 시작한다.
                mediaRecorder.start();

                // setTimeout 메소드를 사용하여 녹음이 시작된지 5분이 경과하면 자동으로 녹음이 멈추고,
                // 이후 녹음을 더 이상 할 수 없도록 막고 초기화와 전송만 가능하도록 한다.
                // 이는 녹음 최대 시간이 5분을 넘지 않도록 하는 것이다.
                // 그리고 recordTimeoutId 변수에 setTimeout 메소드로 반환된 타이머 ID를 저장하여,
                // 타이머가 실행되는 동안 recordTimeoutId로 언제든지 타이머를 취소할 수 있도록 한다.
                recordTimeoutId = setTimeout(function() {
                    // MediaRecorder의 stop() 메소드를 호출하여 녹음을 멈춘다.
                    mediaRecorder.stop();
                    // 초기화 버튼을 활성화한다.
                    btnRecordStart.disabled = false;
                    // 재시작 버튼을 비활성화한다.
                    btnRecordStop.disabled = true;
                    // 전송 버튼을 활성화한다.
                    btnRecordSend.disabled = false;
                }, 300000);
            // catch 메소드는 Promise 객체가 reject된 후 호출된다.
            } catch (error) {
                // 오류가 발생한 경우, 해당 오류를 콘솔에 출력한다.
                console.error(error);
            }
    });
}

// 녹음된 오디오 메시지 전송 메소드
async function sendRecording() {
    // Promise 객체를 생성하여 async/await를 사용해 Blob 객체를 base64 문자열로 인코딩한다.
    // 해당 Promise 객체는 resolve를 호출하여 base64로 인코딩된 오디오 데이터를 반환해 base64Data 변수에 전달한다.
    const base64Data = await new Promise(resolve => {
        // FileReader 객체를 생성한다.
        // FileReader - 웹 API 중 하나로, 클라이언트 측 자바스크립트에서 파일의 내용을 비동기적으로 읽어들일 수 있도록 해주는 객체이다.
        //              이 객체를 이용하여 파일의 내용을 읽을 수 있으며, 이 때 파일의 종류나 형식에 따라 읽어들이는 방식이나 사용 방법이 다를 수 있다.
        const reader = new FileReader();
        // readAsDataURL() 메소드를 사용하여 Blob 객체를 base64 문자열로 변환한다.
        // readAsDataURL() - 메서드는 File 혹은 Blob 객체의 데이터를 읽어와, 해당 데이터를 Data URL 형태로 반환한다.
        //                   이 때 반환된 Data URL은 문자열이며, base64 인코딩된 형태의 데이터를 포함하고 있다.
        reader.readAsDataURL(recordBlob);
        // FileReader 객체에 onloadend 이벤트를 등록하여 파일 읽기 작업이 완료되면 호출될 콜백 메소드를 등록한다.
        reader.onloadend = function() {
            // Promise 객체에서 resolve 메소드를 호출하고,
            // reader.result를 사용하여 readAsDataURL()를 통해 변환한 base64 문자열 데이터를 가져와 반환한다.
            // resolve - Promise 객체에서 성공적으로 실행될 경우에 호출되며, 그 결과 값을 반환한다.
            //           여기서는 reader.result를 반환하므로, 이후 이 Promise 객체를 사용하는 메소드에서 reader.result 값을 받아 처리할 수 있다.
            resolve(reader.result);
        }
    });
    // send(path, header, message)를 사용하여 녹음된 오디오 데이터를 메시지로 전송한다. (모든 녹음된 오디오 데이터는 여기서 메시지를 전송한다.)
    // {metaIdx: metaIdx, writer: metaNickname} - 전송할 메시지의 Header로, Header를 통해 metaIdx와 nickname을 전송한다.
    // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 파라미터를 통해 받는다.
    stomp.send('/pub/meta/studyroom/record', {metaIdx: metaIdx, writer: metaNickname}, base64Data);
    // recordedChunks 배열을 초기화한다.
    recordedChunks = [];
    // 오디오 태그에 작성되있는 소스를 초기화한다.
    record.src = "";
    // 병합한 Blob 녹음 데이터의 URL을 초기화한다.
    recordUrl = "";
}

// 오디오 태그에서 "ended" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// ended - 오디오나 비디오 등의 미디어가 재생이 끝났을 때 발생하는 이벤트이다.
record.addEventListener("ended", function() {
    // 오디오 태그의 재생이 종료되면, 오디오 태그에 작성되어 있는 소스를 그대로 재작성한다.
    // 이는 오디오 재생이 끝나고 다시 재생할 때, 오디오 소스가 초기화되도록 하는 역할을 한다.
    // 이렇게 하는 이유는, 오디오 태그의 재생이 종료되고 나서 다시 오디오 태그가 재생될 때, 병합된 오디오 데이터를 온전히 다시 재생하기 위해서이다.
    // 이렇게 하지 않으면, 오디오 태그의 재생이 종료되고 나서 다시 오디오 태그가 재생될 때, 병합된 오디오 데이터중 마지막 오디오 데이터만 재생된다.
    // 하나의 오디오 데이터는 문제가 없지만, 병합된 여러개의 오디오 데이터는 이런 문제가 발생해 임시방편으로 찾아낸 방법이다.
    record.src = record.src;
});

// 시작 및 초기화 버튼에서 "click" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// click - 마우스나 터치 디바이스의 클릭이나 탭을 할 때 발생하는 이벤트이다.
btnRecordStart.addEventListener("click", function() {
    // 시작 버튼 이름을 체크한다.
    // "녹음 시작" 버튼을 클릭한 경우
    if ( btnRecordStart.value == "녹음 시작" ) {
        // 녹음 시작 메소드를 호출하여 녹음을 시작한다.
        startRecording();
        // 시작 버튼을 초기화 버튼으로 변경한다.
        btnRecordStart.value = "녹음 초기화";
        // 초기화 버튼을 비활성화한다.
        btnRecordStart.disabled = true;
        // 정지 버튼을 활성화한다.
        btnRecordStop.disabled = false;
        // 전송 버튼을 비활성화한다.
        btnRecordSend.disabled = true;
    // "녹음 초기화" 버튼을 클릭한 경우
    } else {
        // 초기화 버튼을 시작 버튼으로 변경한다.
        btnRecordStart.value = "녹음 시작";
        // 시작 버튼을 활성화한다.
        btnRecordStart.disabled = false;
        // 재시작 버튼을 정지 버튼으로 변경한다.
        btnRecordStop.value = "녹음 정지";
        // 정지 버튼을 비활성화한다.
        btnRecordStop.disabled = true;
        // 전송 버튼을 비활성화한다.
        btnRecordSend.disabled = true;
        // recordedChunks 배열을 초기화한다.
        recordedChunks = [];
        // 현재 오디오 태그에 작성되있는 소스를 초기화한다.
        record.src = "";
    }
});

// 정지 및 재시작 버튼에서 "click" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// click - 마우스나 터치 디바이스의 클릭이나 탭을 할 때 발생하는 이벤트이다.
btnRecordStop.addEventListener("click", function() {
    // 정지 버튼 이름을 체크한다.
    // "녹음 정지" 버튼을 클릭한 경우
    if ( btnRecordStop.value == "녹음 정지" ) {
        // recordTimeoutId에 저장된 setTimeout ID를 사용하여 실행 중인 타이머를 취소한다.
        clearTimeout(recordTimeoutId);
        // MediaRecorder의 stop() 메소드를 호출하여 녹음을 멈춘다.
        mediaRecorder.stop();
        // 시작 버튼을 초기화 버튼으로 변경한다.
        btnRecordStart.value = "녹음 초기화";
        // 초기화 버튼을 활성화한다.
        btnRecordStart.disabled = false;
        // 정지 버튼을 재시작 버튼으로 변경한다.
        btnRecordStop.value = "녹음 재시작";
        // 재시작 버튼을 활성화한다.
        btnRecordStop.disabled = false;
        // 전송 버튼을 활성화한다.
        btnRecordSend.disabled = false;
    // "녹음 재시작" 버튼을 클릭한 경우
    } else {
        // 오디오 태그에 작성되있는 소스를 초기화한다.
        record.src = "";
        // 병합한 Blob 녹음 데이터의 URL을 초기화한다.
        recordUrl = "";
        // 녹음 시작 메소드를 호출하여 녹음을 재시작한다.
        startRecording();
        // 초기화 버튼을 비활성화한다.
        btnRecordStart.disabled = true;
        // 재시작 버튼을 정지 버튼으로 변경한다.
        btnRecordStop.value = "녹음 정지";
        // 정지 버튼을 활성화한다.
        btnRecordStop.disabled = false;
        // 전송 버튼을 비활성화한다.
        btnRecordSend.disabled = true;
    }
});

// 전송 버튼에서 "click" 이벤트가 발생하면 해당 이벤트를 처리하는 콜백 메소드를 등록한다.
// click - 마우스나 터치 디바이스의 클릭이나 탭을 할 때 발생하는 이벤트이다.
btnRecordSend.addEventListener("click", function() {
    // 녹음된 오디오 메시지 전송 메소드를 호출하여 녹음된 오디오를 메시지로 전송한다.
    sendRecording();
    // 초기화 버튼을 시작 버튼으로 변경한다.
    btnRecordStart.value = "녹음 시작";
    // 시작 버튼을 활성화한다.
    btnRecordStart.disabled = false;
    // 재시작 버튼을 정지 버튼으로 변경한다.
    btnRecordStop.value = "녹음 정지";
    // 정지 버튼을 비활성화한다.
    btnRecordStop.disabled = true;
    // 전송 버튼을 비활성화한다.
    btnRecordSend.disabled = true;
    // recordedChunks 배열을 초기화한다.
    recordedChunks = [];
});
///////////////////////////////////////////////// 페이지 이탈 구역 /////////////////////////////////////////////////
// 1. 페이지를 이탈하는 기능을 실행할 경우 발생하는 이벤트 핸들러 등록
// beforeunload - 탭 닫기, 윈도우 닫기, 페이지 닫기, 뒤로가기, 버튼, location.href, 새로고침 등 해당 페이지를 벗어나는 기능을 실행할 경우 항상 실행된다.
window.addEventListener("beforeunload", handleBeforeUnload);
// beforeunload 핸들러 메소드
function handleBeforeUnload(event) {
    // beforeunload 이벤트를 명시적으로 처리하지 않은 경우, 해당 이벤트에 기본 동작을 실행하지 않도록 지정한다.
    event.preventDefault();
    // beforeunload 경고창을 띄워준다. - 따로 메시지 작성을 안한 이유는 각 브라우저마다 기본으로 잡혀있는 메시지가 표시되기 때문이다.
    event.returnValue = "";
}

// 2. beforeunload 이벤트에서 반환한 경고창에 따라 <body>태그에 작성한 unload 이벤트에서 확인 및 취소를 체크하고, 확인을 누른 경우에만 지정한 메소드로 이동시킨다.
// unload - 어떤 방식으로든 페이지를 이탈하면 항상 실행된다.

// 3. 페이지 이탈 후 퇴장 메시지 전송 메소드 - unload에서 지정한 메소드
function exit() {
    // 방장 닉네임과 퇴장 유저 닉네임이 같은지 체크한다.
    // 닉네임이 같은 경우 - 방장 퇴장
    if ( metaNickname == metaMaster ) {
        // send(path, header, message)로 퇴장 메시지를 전송한다. (방장이 퇴장할때 딱 한번만 전송한다.)
        // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
        // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
        stomp.send('/pub/meta/studyroom/exit', {}, JSON.stringify({type: "delete", metaIdx: metaIdx, metaTitle: metaTitle, writer: metaNickname, metaRecruitingPersonnel: metaRecruitingPersonnel, master: metaMaster}));
    // 닉네임이 다른 경우 - 유저 퇴장
    } else {
        // send(path, header, message)로 퇴장 메시지를 전송한다. (퇴장할때 딱 한번만 전송한다.)
        // JSON.stringify({json형식}) - JavaScript 값이나 객체를 JSON 문자열로 변환한다.
        // 여기서 전송한 메시지를 StompChatController에 @MessageMapping이 DTO를 통해 받는다.
        stomp.send('/pub/meta/studyroom/exit', {}, JSON.stringify({type: "exit", metaIdx: metaIdx, metaTitle: metaTitle, writer: metaNickname, metaRecruitingPersonnel: metaRecruitingPersonnel}));
    }
}