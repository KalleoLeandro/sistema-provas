import { Component,Input,Output,EventEmitter } from '@angular/core';
@Component({selector:'visual-question-navigation',templateUrl:'./question-navigation.html',styleUrl:'./question-navigation.css'})
export class QuestionNavigationComponent {
 @Input() answered:number[]=[];@Input() total=10;@Output() selected=new EventEmitter<number>();
 get numbers(){return Array.from({length:this.total},(_,i)=>i+1);}
}
