import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import 'rxjs/add/operator/map';
import { Observable } from 'rxjs/Observable';
import { HttpProvider } from '../providers/http-provider';
import { AuthenticationProvider } from '../providers/authentication-provider';

/*
  Generated class for the UsersProvider provider.

  See https://angular.io/docs/ts/latest/guide/dependency-injection.html
  for more info on providers and Angular 2 DI.
*/
@Injectable()
/**
 * Prepares HTTP Request for the Users functionality
 */
export class UsersProvider {

  /**
   * Dependency Injection
   * @param http - angular2 standart http module
   * @param httpProvider - the http provider that actually sends the requests
   * @param auth - authentication provider
     */
  constructor(public http: Http, private httpProvider: HttpProvider, private auth: AuthenticationProvider) {
  }

  /**
   * Searches for users with a given name
   * @param name
   * @returns {Observable<any>} - the response from the server
     */
  getUsersWithName(name: string) : Observable<any>{
    let searchOptions = { q: name };
    return this.httpProvider.get(this.httpProvider.ROUTES.users + "/directory", searchOptions);
  }

  /**
   * Gets the information for a single user
   * @param UserID - the user id
   * @returns {Observable<any>} - the response from the server
     */
  getUser(UserID: number) : Observable<any>{
    return this.httpProvider.get(this.httpProvider.ROUTES.users + "/directory/" + UserID);
  }

  /**
   * Gets the information for the current user
   * @returns {Observable<any>} - the response from the server
     */
  getMe() : Observable<any>{
    return this.httpProvider.get(this.httpProvider.ROUTES.users);
  }

  /**
   * Changes the profile information of the current user
   * @param data - the new data
   * @returns {Observable<any>} - the response from the server
     */
  changeProfileInfo(data: any) : Observable<any>{
    let body = {
      nutzerName: data.username,
      nutzerEmail: data.email,
      adresse: {
        plz: data.zip,
        stadt: data.city,
        strasse: data.street,
        hausNummer: data.streetNr
      }
    };
    return this.httpProvider.put(this.httpProvider.ROUTES.users, body);
  }

  /**
   * Changes the password of the current user
   * @param data - the users data
   * @returns {Observable<any>} - the response from the server
     */
  changePassword(data: any) : Observable<any>{
    return this.httpProvider.put(this.httpProvider.ROUTES.users + "/password", {oldPassword: data.oldPassword, newPassword: data.newPassword, nutzerName: data.username, nutzerEmail: data.email});
  }
}
