import { Component,Input,forwardRef,ChangeDetectorRef,inject } from '@angular/core';
import { FormsModule,ControlValueAccessor,NG_VALUE_ACCESSOR } from '@angular/forms';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
@Component({selector:'visual-date-field',imports:[FormsModule,NzDatePickerModule],templateUrl:'./date-field.html',styleUrl:'./date-field.css',providers:[{provide:NG_VALUE_ACCESSOR,useExisting:forwardRef(()=>DateFieldComponent),multi:true}]})
export class DateFieldComponent implements ControlValueAccessor {
 @Input() inputId='';@Input() showTime=false;@Input() invalid=false;@Input() placeholder='Selecione a data';
 value:Date|null=null;disabled=false;private readonly cd=inject(ChangeDetectorRef);
 private onChange:(value:string)=>void=()=>{};private onTouched:()=>void=()=>{};
 get format(){return this.showTime?'dd/MM/yyyy HH:mm':'dd/MM/yyyy';}
 writeValue(value:string|null){const date=value?new Date(value):null;this.value=date&&!Number.isNaN(date.getTime())?date:null;this.cd.markForCheck();}
 registerOnChange(fn:(value:string)=>void){this.onChange=fn;}
 registerOnTouched(fn:()=>void){this.onTouched=fn;}
 setDisabledState(value:boolean){this.disabled=value;this.cd.markForCheck();}
 changed(value:Date|null){this.value=value;this.onChange(value?value.toISOString():'');this.onTouched();}
 touched(){this.onTouched();}
}
