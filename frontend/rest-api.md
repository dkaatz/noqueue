# Frontend - REST API

**Bold** - muss beim Frontend angepasst werden  
==Highlighted== - fehlt beim Backend

## AuthenticationProvider


Name          	| Route            | Request                    | Response | Calling Files 
-----------------|------------------|-------------------------|----------|---
Login		 	 	| POST /auth       | nutzerName<br>password | token | login.ts
Signup          | POST /anwender   | nutzerName<br>nutzerEmail<br>password|token | signup.ts 


## UsersProvider

Name          	| Route            | Request                    | Response | Calling Files
-----------------|------------------|-------------------------|----------|---
Get Users with name | GET /anwender/directory<br>?q=name | q=name| [ id<br>nutzerName<br>nutzerEmail ] | coworkers.ts
**Get User** | **GET /anwender/directory/:id** | | **id<br>nutzerName<br>nutzerEmail**
Get Me | **GET /anwender** | | [ **id**<br>nutzerName<br>nutzerEmail<br>adresse:{<br>**id,straße**,hausNummer,plz,stadt<br>}] | edit-profile.ts
Change Profile Info | **PUT /anwender** | [ nutzerName<br>nutzerEmail<br>{adresse} ] | | edit-profile.ts
Change Password | **PUT /anwender/password** | password<br>**nutzerName<br>nutzerEmail** | | edit-password.ts


## ShopsProvider

Name          	| Route            | Request                    | Response | Calling Files 
-----------------|------------------|-------------------------|----------|---
==Get Shops== | GET /betrieb<br>?size=5<br>&page=1<br>&q=filter<br>&radius=2 | size<br>page<br>q<br>radius | [ **id**<br>name<br>kontaktEmail<br>tel<br>**oeffnungszeiten**<br>{adresse}<br>distanz ] | dashboard.ts<br>shops.ts
**~~Get Shops Nearby~~** | | | 
==Get My Shops== | **GET /anwender/betrieb** | | [ **id**<br>name<br>kontaktEmail<br>tel<br>**oeffnungszeiten**<br>{adresse}<br>distanz ] | dashboard.ts<br>my-shops.ts
Get Shop | GET /betrieb/:id | | **id**<br>name<br>kontaktEmail<br>tel<br>**oeffnungszeiten**<br>{adresse}<br>distanz | my-shop-single.ts<br>shop-info.ts<br>shop-single.ts
Create Shop | POST /betrieb | name<br>kontaktEmail<br>tel<br>**oeffnungszeiten**<br>{adresse} | | shop-info.ts
Edit Shop | PUT /betrieb/:id | name<br>kontaktEmail<br>tel<br>**oeffnungszeiten**<br>{adresse} | | shop-info.ts
Get Employees | GET /betrieb/:id/mitarbeiter | | [ **~~betriebID~~**<br>**anwenderId**<br>anwesend<br>**~~nutzerName~~** ] | my-shop-single.ts<br>service-single.ts<br>shop-single.ts
Get Managers | GET /betrieb/:id/leiter | | [ **~~betriebID~~**<br>**anwenderId**<br>anwesend<br>**~~nutzerName~~** ] | my-shop-single.ts
Hire Employee | POST /betrieb/:id/mitarbeiter | **anwenderId<br>betriebId<br>anwesend** | | coworkers.ts<br>my-shop-single.ts
Hire Manager | POST /betrieb/:id/leiter | **anwenderId<br>betriebId<br>anwesend** | | coworkers.ts<br>my-shop-single.ts
Fire Employee | DELETE /betrieb/:id/mitarbeiter/:userID | | | my-shop-single.ts 
Fire Manager | DELETE /betrieb/:id/leiter/:userID | | | my-shop-single.ts



## ServicesProvider

Name          	| Route            | Request                    | Response | Calling Files
-----------------|------------------|-------------------------|----------|---
Get Services For | GET /betrieb/:id/dienstleistung | | [ serviceID<br>**~~betriebID~~**<br>typ<br>**~~dauer~~**<br>**~~kommentar~~** ] | my-shop-single.ts<br>shop-single.ts
Get Service | GET /betrieb/:id/dienstleistung/:serviceID | | serviceID<br>betriebID<br>typ<br>dauer<br>**kommentar** | service-info.ts<br>service-single.ts
Get All Service Types | **POST /dlts** ?? | | [ *strings* ] | service-info.ts
Create Service | POST /betrieb/:id/dienstleistung | dauer<br>typ<br>**kommentar** | | service-info.ts
Edit Service | PUT /betrieb/:id/dienstleistung/:serviceID | dauer<br>typ<br>**kommentar** | | service-info.ts




## QueuesProvider

Name          	| Route            | Request                    | Response | Calling Files
-----------------|------------------|-------------------------|----------|---
==Get My Queues== | **GET /anwender/queues** | | [ queueID<br>name (*betrieb*) <br>dienstleistung<br>wsoffen ] | dashboard.ts<br>my-queues.ts
==Get Queue==     | **GET /anwender/queues/:queueID** | | queueID<br>betriebID<br>serviceID<br>**~~name~~**<br>dienstleistung<br>wsoffen<br>[ kunden:<br> {nutzerName<br> userID<br> uhrzeit}<br>] | my-queue-single.ts
==Get My Queue Position== | **GET /anwender/queueposition** | | uhrzeit<br>mitarbeiterID<br>betriebID<br>positionID | dashboard.ts<br>my-queue-position.ts
==Line Up== | POST /queues |userID<br>dienstleistungID<br>mitarbeiterID | | service-single.ts
==Leave== | DELETE /queues/:queueID | | | my-queue.position.ts