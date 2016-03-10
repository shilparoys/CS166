CREATE INDEX  userListAIndex
ON USER_LIST
(list_id);

CREATE INDEX usrIndex
ON USR
(login);
 
CREATE INDEX userListContains
ON USER_LIST_CONTAINS
(list_id);

CREATE INDEX  userListBIndex
ON USER_LIST_CONTAINS
(list_member);

CREATE INDEX chatIndex
ON CHAT
(chat_id);

CREATE INDEX chatListAIndex
ON CHAT_LIST
(chat_id);

CREATE INDEX chatListBIndex
ON CHAT_LIST
(member);

CREATE INDEX messageIndex
ON MESSAGE
(msg_id);

